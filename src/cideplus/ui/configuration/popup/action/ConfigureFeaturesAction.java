package cideplus.ui.configuration.popup.action;

import java.io.IOException;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.dialogs.ConfigureFeaturesDialog;

public class ConfigureFeaturesAction implements IObjectActionDelegate {

	private Shell shell;
	private IJavaProject project;
	
	/**
	 * Constructor for Action1.
	 */
	public ConfigureFeaturesAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		ConfigureFeaturesDialog configureFeaturesDialog = new ConfigureFeaturesDialog(shell, project);
		try {
			configureFeaturesDialog.configure();
		} catch (IOException e) {
			MessageDialog.openError(
					shell,
					"Featurer",
					"Could not configure the project to use Featurer. Error reading file "+FeaturesConfigurationUtil.FEATURES_FILE+". "+e.getMessage()+".");
		} catch (Exception e) {
			MessageDialog.openError(
					shell,
					"Featurer",
					"Could not configure the project to use Featurer. \n"+e.getMessage()+".");
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IJavaProject){
			project = (IJavaProject) selection;
		} else if (selection instanceof IStructuredSelection){
			project = (IJavaProject) ((IStructuredSelection)selection).getFirstElement();
		}
	}

}
