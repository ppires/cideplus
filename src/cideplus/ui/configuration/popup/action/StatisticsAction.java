package cideplus.ui.configuration.popup.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import cideplus.model.ASTNodeReference;
import cideplus.model.Feature;
import cideplus.ui.configuration.ICompilationUnitFeaturesManager;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.IFeaturesManager;

public class StatisticsAction implements IObjectActionDelegate {

	private IJavaProject project;
	private Shell shell;
	
	private class Statitics {
		
		Feature feature;
		
		int bytesCounted = 0;
		int nodesCounted = 0;
		
		int nodesCountedNonOverriden = 0;
		int bytesCountedNonOverriden = 0;
		
		public boolean isForFeature(Feature feature){
			if(this.feature == null){
				return true;
			} else {
				return this.feature.equals(feature);
			}
		}
		
		@Override
		public String toString() {
			String result;
			if(feature != null){
				result = "Feature "+feature.getName()+"\n";
			} else {
				result = "Totals \n";
			}
			result += "    Bytes (Non overriden/total): "+bytesCountedNonOverriden+" / " +bytesCounted+"\n" +
					  "    Nodes (Non overriden/total): "+nodesCountedNonOverriden+" / " +nodesCounted;
			return  result;
		}
	}


	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}
	
	public void run(IAction action) {

		try {
			FeaturesConfigurationUtil.clean();
			List<Statitics> doStatitics = doStatitics(new NullProgressMonitor());
			StringBuilder result = new StringBuilder();
			for (Statitics statitics : doStatitics) {
				result.append(statitics);
				result.append("\n");
			}
			MessageDialog.openInformation(shell, "Statitics", result.toString());
		} catch (Exception e) {
			MessageDialog.openError(shell, "Erro", e.getMessage());
		}

	}
	
	private List<Statitics> doStatitics(final IProgressMonitor monitor) throws CoreException {
		final List<Statitics> statiticsList = new ArrayList<StatisticsAction.Statitics>();
		statiticsList.add(new Statitics());//adicionar uma estatistica para o conjunto geral
		final IFeaturesManager featuresManager = FeaturesConfigurationUtil.getFeaturesManager(project.getProject());
		Set<Feature> features;
		try {
			features = featuresManager.getFeatures();
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		for (Feature feature : features) {
			//para cada feature.. gerar uma estatistica
			Statitics s = new Statitics();
			s.feature = feature; 
			statiticsList.add(s);
		}
		project.getProject().accept(new IResourceVisitor() {
			
			public boolean visit(IResource resource) throws CoreException {
				if(resource instanceof IFolder || resource instanceof IProject){
					return true;
				}
				if(resource.getName().endsWith("feat") && !resource.getName().equals(FeaturesConfigurationUtil.FEATURES_FILE)){
					IPath path = resource.getProjectRelativePath().removeFileExtension().addFileExtension("java");
					IFile file = (IFile) project.getProject().findMember(path);
					if(file != null && file.exists()){
						//System.out.println(file);
						IJavaElement java = JavaCore.create(file, project);
						if(java instanceof ICompilationUnit){
							try {
								ICompilationUnitFeaturesManager manager = featuresManager.getManagerForFile((ICompilationUnit)java);
								for (Statitics statitics : statiticsList) {
									//gerar todas as estatisticas para o compilationUnit
									statitics.bytesCounted += countBytes(manager, statitics);
									statitics.nodesCounted += countNodes(manager, statitics);
									statitics.bytesCountedNonOverriden += countBytesNonOverriden(manager, statitics);
									statitics.nodesCountedNonOverriden += countNodesNonOverriden(manager, statitics);
								}
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						} else {
							throw new RuntimeException("No java element found for "+file);
						}
					}
					//FeaturesConfigurationUtil.getFeaturesManager(project.getProject()).getManagerForFile(file)
					//statitics.bytesCounted += countBytes(file.getContents());
				}
				return false;
			}

		});
		return statiticsList;
	}
	
	private int countNodesNonOverriden(ICompilationUnitFeaturesManager manager, Statitics statitics) {
		return countNodes(manager, getNonOverridenNodeReferences(manager), statitics);
	}
	
	private int countBytesNonOverriden(ICompilationUnitFeaturesManager manager, Statitics statitics) {
		return countBytes(manager, getNonOverridenNodeReferences(manager), statitics);
	}

	private Set<ASTNodeReference> getNonOverridenNodeReferences(ICompilationUnitFeaturesManager manager) {
		Set<ASTNodeReference> nodeReferences = copy(manager.getNodeReferences());
		for (Iterator<ASTNodeReference> iterator = nodeReferences.iterator(); iterator.hasNext();) {
			ASTNodeReference astNodeReference = iterator.next();
			for (ASTNodeReference astNodeReference2 : nodeReferences) {
				if(astNodeReference.isChild(astNodeReference2)){
					iterator.remove();
					break;
				}
			}
		}
		return nodeReferences;
	}
	
	private Set<ASTNodeReference> copy(Set<ASTNodeReference> nodeReferences) {
		Set<ASTNodeReference> set = new HashSet<ASTNodeReference>();
		set.addAll(nodeReferences);
		return set;
	}

	private int countNodes(ICompilationUnitFeaturesManager manager, Statitics statitics) {
		return countNodes(manager, manager.getNodeReferences(), statitics);
	}

	private int countNodes(ICompilationUnitFeaturesManager manager, Set<ASTNodeReference> nodeReferences, Statitics statitics) {
		int count = 0;
		for (ASTNodeReference astNodeReference : nodeReferences) {
			Set<Feature> features = manager.getFeatures(astNodeReference);
			for (Feature feature : features) {
				if(statitics.isForFeature(feature)){
					//se as estatiticas que estao sendo calculadas, sï¿½o para determinada feature, aplicar o valor e passar para o proximo nï¿½
					count += 1;
					break;
				}
			}
		}
		return count;
	}
	
	private int countBytes(ICompilationUnitFeaturesManager manager, Statitics statitics) {
		return countBytes(manager, manager.getNodeReferences(), statitics);
	}

	private int countBytes(ICompilationUnitFeaturesManager manager, Set<ASTNodeReference> nodeReferences, Statitics statitics) {
		int count = 0;
		for (ASTNodeReference astNodeReference : nodeReferences) {
			Set<Feature> features = manager.getFeatures(astNodeReference);
			for (Feature feature : features) {
				if(statitics.isForFeature(feature)){
					//se as estatiticas que estao sendo calculadas, sï¿½o para determinada feature, aplicar o valor e passar para o proximo nï¿½
					count += astNodeReference.getByteCount();
					break;
				}
			}
		}
		return count;
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IJavaProject){
			project = (IJavaProject) selection;
		} else if (selection instanceof IStructuredSelection){
			project = (IJavaProject) ((IStructuredSelection)selection).getFirstElement();
		}
	}

}
