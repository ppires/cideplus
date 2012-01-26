package cideplus.ui.export.action;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.swt.widgets.Shell;

import cideplus.model.ASTNodeReference;
import cideplus.model.Feature;
import cideplus.model.FeaturerException;
import cideplus.ui.configuration.CompilationUnitFeaturesManager;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.FeaturesManager;

public class Exporter {

	private Shell shell;
	private IJavaProject project;
	private int projectJavaCount;
	private FeaturesManager featuresManager;
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
	 * Exporta os arquivos para um mapa com o nome e conteúdo de cada arquivo exportado.<BR>
	 * Para cada arquivo é adicionado 3 ao trabalho do monitor<BR>
	 * O método getFileCount retorna o número de arquivos encontrados, esse valor é o mesmo do número de progressos desse método (mas cada progresso conta 3 unidades).
	 * Os métodos beginTask e done do objeto monitor, devem ser chamados fora desse método. Um antes e o outro depois respectivamente.
	 * @param monitor
	 * @return
	 * @throws CoreException
	 * @throws FeaturerException 
	 * @throws IOException 
	 */
	public Map<String, byte[]> getExportedFiles(IProgressMonitor monitor) throws CoreException, IOException, FeaturerException {
		IPackageFragmentRoot[] allPackageFragmentRoots = project.getAllPackageFragmentRoots();
		Map<String, byte[]> result = new LinkedHashMap<String, byte[]>();
		for (IPackageFragmentRoot iPackageFragmentRoot : allPackageFragmentRoots) {
			if(iPackageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE){
				//todos os package roots que são source folders...
				monitor.setTaskName("Exporting... "+iPackageFragmentRoot.getElementName());
				IJavaElement[] children = iPackageFragmentRoot.getChildren();
				checkChildren(result, children, monitor);
			}
		}
		return result;
	}

	private void checkChildren(Map<String, byte[]> result, IJavaElement[] children, IProgressMonitor monitor) throws CoreException, IOException, FeaturerException {
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
	 * @throws FeaturerException 
	 * @throws IOException 
	 */
	private void exportJavaFile(Map<String, byte[]> result,	ICompilationUnit comp, IProgressMonitor monitor) throws IOException, FeaturerException, CoreException {
		if(monitor.isCanceled()){
			throw new OperationCanceledException();
		}
		monitor.setTaskName("Exporting... "+comp.getParent().getElementName()+"."+stripExtension(comp.getElementName()));
		CompilationUnitFeaturesManager manager = featuresManager.getManagerForFile(comp);
		Set<ASTNodeReference> nodesToRemove = getNodesToRemove(manager);
		Set<ASTNodeReference> nodeReferences = removeOverridenAndOrder(nodesToRemove);
		String source = comp.getSource();
		for (Iterator<ASTNodeReference> iterator = nodeReferences.iterator(); iterator.hasNext();) {
			ASTNodeReference astNodeReference = iterator.next();
			//elimina do source o código do nó que não será exportado
			source = source.substring(0, astNodeReference.getOffset()) +
					 source.substring(astNodeReference.getOffset() + astNodeReference.getByteCount(), source.length());
		}
		//CompilationUnit ast = cideplus.automation.Util.getAst(comp, false);
		result.put(comp.getPath().toString(), source.getBytes());
		monitor.worked(3);
	}

	/**
	 * Retorna a lista de nós que devem ser removidos
	 * @param manager
	 * @return
	 */
	protected Set<ASTNodeReference> getNodesToRemove(CompilationUnitFeaturesManager manager) {
		Set<ASTNodeReference> allNodeReferences = manager.getNodeReferences();
		Set<ASTNodeReference> nodesToRemove = new HashSet<ASTNodeReference>();
		for (Iterator<ASTNodeReference> iterator = allNodeReferences.iterator(); iterator.hasNext();) {
			ASTNodeReference astNodeReference = iterator.next();
			//iremos remover os items que não serão exportados... é importante que os nós estejam ordenados por offset decrescente
			Set<Feature> nodeFeatures = manager.getFeatures(astNodeReference);
			nodeFeatures.removeAll(exportedFeatures);
			if(nodeFeatures.size() > 0){
				//se ao remover da lista de features do nó.. a lista de features a ser exportada..
				//sobrar alguma feature, significa que o nó tem uma feature que não está sendo exportada...
				//então vamos remover esse nó
				nodesToRemove.add(astNodeReference);
			}
		}
		return nodesToRemove;
	}

	/**
	 * Remove as features que estão se sobrescrevendo... é utilizado o offset e o length para o cálculo.
	 * Ordena os nós em ordem decrescente de offset. Ou seja, offsets maiores vem primeiro.
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
