package cideplus.ui.configuration.popup.action;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import cideplus.FeaturerPlugin;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.presentation.FeaturesMarker;

public class CleanProjectAction  implements IObjectActionDelegate {

	private IJavaProject project;
	private Shell shell;


	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	@Override
	public void run(IAction action) {
		MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
		dialog.setText("Warning!");
		dialog.setMessage("This will erase ALL features markings.\nDo you really want to do this?");
		int returnCode = dialog.open();
		if (returnCode == SWT.OK) {
			new Job("Clean project") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					monitor.beginTask("Cleaning project...", IProgressMonitor.UNKNOWN);
					try {
						doClean(monitor);
					} catch (CoreException e) {
						return new Status(IStatus.ERROR, FeaturerPlugin.PLUGIN_ID, e.getMessage());
					} finally {
						FeaturesConfigurationUtil.updateEditors(shell.getDisplay(), null);
						FeaturesConfigurationUtil.clean();
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	private void doClean(final IProgressMonitor monitor) throws CoreException {

		// Delete all markers associated with the project
		for (IMarker marker : FeaturesMarker.findAllRelatedMarkers(project.getProject())) {
			marker.delete();
			monitor.worked(1);
		}

		project.getProject().accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if(resource instanceof IFolder || resource instanceof IProject){
					return true;
				}
				if(resource.getName().endsWith("feat") && !resource.getName().equals(FeaturesConfigurationUtil.FEATURES_FILE)){
					monitor.setTaskName("Cleaning project... deleting feature file " + resource.getName());
					resource.delete(true, new NullProgressMonitor());
					monitor.worked(5);
				}
				return false;
			}
		});
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IJavaProject){
			project = (IJavaProject) selection;
		} else if (selection instanceof IStructuredSelection){
			project = (IJavaProject) ((IStructuredSelection)selection).getFirstElement();
		}
	}
}
