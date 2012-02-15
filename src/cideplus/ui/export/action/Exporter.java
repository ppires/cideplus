package cideplus.ui.export.action;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.widgets.Shell;

import cideplus.model.ASTNodeReference;
import cideplus.model.Feature;
import cideplus.model.exceptions.FeatureNotFoundException;
import cideplus.ui.configuration.ICompilationUnitFeaturesManager;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.IFeaturesManager;

public class Exporter {

	private Shell shell;
	private IJavaProject project;
	private int projectJavaCount;
	private IFeaturesManager featuresManager;
	private Set<Feature> exportedFeatures;

	public Exporter(Shell shell, IJavaProject project, Set<Feature> features) {
		this.shell = shell;
		this.project = project;
		this.exportedFeatures = features;
		featuresManager = FeaturesConfigurationUtil.getFeaturesManager(project.getProject());
	}

	public int getFileCount(IProgressMonitor monitor) throws CoreException{
		monitor.beginTask("Preparing to export...", IProgressMonitor.UNKNOWN);
		countProjectFiles();
		monitor.done();
		return projectJavaCount;
	}

	/**
	 * Exporta os arquivos para um mapa com o nome e conte�do de cada arquivo exportado.<BR>
	 * Para cada arquivo � adicionado 3 ao trabalho do monitor<BR>
	 * O m�todo getFileCount retorna o n�mero de arquivos encontrados, esse valor � o mesmo do n�mero de progressos desse m�todo (mas cada progresso conta 3 unidades).
	 * Os m�todos beginTask e done do objeto monitor, devem ser chamados fora desse m�todo. Um antes e o outro depois respectivamente.
	 * @param monitor
	 * @return
	 * @throws CoreException
	 * @throws FeatureNotFoundException
	 * @throws IOException
	 */
	public Map<String, byte[]> getExportedFiles(IProgressMonitor monitor) throws CoreException, IOException, FeatureNotFoundException {
		IPackageFragmentRoot[] allPackageFragmentRoots = project.getAllPackageFragmentRoots();
		Map<String, byte[]> result = new LinkedHashMap<String, byte[]>();
		for (IPackageFragmentRoot iPackageFragmentRoot : allPackageFragmentRoots) {
			if(iPackageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE){
				//todos os package roots que s�o source folders...
				monitor.setTaskName("Exporting... "+iPackageFragmentRoot.getElementName());
				IJavaElement[] children = iPackageFragmentRoot.getChildren();
				checkChildren(result, children, monitor);
			}
		}
		return result;
	}

	private void checkChildren(Map<String, byte[]> result, IJavaElement[] children, IProgressMonitor monitor) throws CoreException, IOException, FeatureNotFoundException {
		for (IJavaElement iJavaElement : children) {
			if(iJavaElement instanceof ICompilationUnit){
				exportJavaFile(result, (ICompilationUnit)iJavaElement, monitor);
			} else {
				if(iJavaElement instanceof IPackageFragment){
					checkChildren(result, ((IPackageFragment)iJavaElement).getChildren(), monitor);
				}
			}
		}
	}

	/**
	 * Exporta determinado arquivo Java para o mapa de arquivos
	 * @param result
	 * @param iJavaElement
	 * @param monitor
	 * @throws CoreException
	 * @throws FeatureNotFoundException
	 * @throws IOException
	 */
	private void exportJavaFile(Map<String, byte[]> result,	ICompilationUnit comp, IProgressMonitor monitor) throws IOException, FeatureNotFoundException, CoreException {
		if(monitor.isCanceled()){
			throw new OperationCanceledException();
		}
		monitor.setTaskName("Exporting... "+comp.getParent().getElementName()+"."+stripExtension(comp.getElementName()));
		ICompilationUnitFeaturesManager manager = featuresManager.getManagerForFile(comp);
		Set<ASTNodeReference> nodesToRemove = getNodesToRemove(manager);
		Set<ASTNodeReference> nodeReferences = removeOverridenAndOrder(nodesToRemove);
		String source = comp.getSource();
		for (Iterator<ASTNodeReference> iterator = nodeReferences.iterator(); iterator.hasNext();) {
			ASTNodeReference astNodeReference = iterator.next();
			//elimina do source o c�digo do n� que n�o ser� exportado
			source = source.substring(0, astNodeReference.getOffset()) +
					source.substring(astNodeReference.getOffset() + astNodeReference.getByteCount(), source.length());
		}
		//CompilationUnit ast = cideplus.automation.Util.getAst(comp, false);
		result.put(comp.getPath().toString(), source.getBytes());
		monitor.worked(3);
	}

	/**
	 * Retorna a lista de n�s que devem ser removidos
	 * @param manager
	 * @return
	 */
	protected Set<ASTNodeReference> getNodesToRemove(ICompilationUnitFeaturesManager manager) {
		Set<ASTNodeReference> allNodeReferences = manager.getNodeReferences();
		Set<ASTNodeReference> nodesToRemove = new HashSet<ASTNodeReference>();
		for (Iterator<ASTNodeReference> iterator = allNodeReferences.iterator(); iterator.hasNext();) {
			ASTNodeReference astNodeReference = iterator.next();
			//iremos remover os items que n�o ser�o exportados... � importante que os n�s estejam ordenados por offset decrescente
			Set<Feature> nodeFeatures = manager.getFeatures(astNodeReference);
			nodeFeatures.removeAll(exportedFeatures);
			if(nodeFeatures.size() > 0){
				//se ao remover da lista de features do n�.. a lista de features a ser exportada..
				//sobrar alguma feature, significa que o n� tem uma feature que n�o est� sendo exportada...
				//ent�o vamos remover esse n�
				nodesToRemove.add(astNodeReference);
			}
		}
		return nodesToRemove;
	}

	/**
	 * Remove as features que est�o se sobrescrevendo... � utilizado o offset e o length para o c�lculo.
	 * Ordena os n�s em ordem decrescente de offset. Ou seja, offsets maiores vem primeiro.
	 * @param nodeReferences
	 * @return
	 */
	private Set<ASTNodeReference> removeOverridenAndOrder(Set<ASTNodeReference> nodeReferences) {
		TreeSet<ASTNodeReference> result = new TreeSet<ASTNodeReference>(new Comparator<ASTNodeReference>() {
			public int compare(ASTNodeReference o1, ASTNodeReference o2) {
				return o2.getOffset() - o1.getOffset();//ordem decrescente de offset
			}
		});
		result.addAll(nodeReferences);
		for (Iterator<ASTNodeReference> iterator1 = result.iterator(); iterator1.hasNext();) {
			ASTNodeReference astNodeReference1 = iterator1.next();
			for (ASTNodeReference astNodeReference2 : result) {
				if(astNodeReference1.isChild(astNodeReference2)){
					iterator1.remove();
					break;
				}
			}
		}
		return result;
	}


	private void countProjectFiles() throws CoreException {
		projectJavaCount = 0;
		project.getProject().accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if(resource.getName().endsWith(".java")){
					projectJavaCount++;
				}
				return true;
			}
		});
	}

	private String stripExtension(String elementName) {
		return elementName.substring(0, elementName.indexOf("."));
	}

}
