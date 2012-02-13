package cideplus.ui.configuration.popup.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.utils.PluginUtils;

public class RefreshFeaturesAction implements IObjectActionDelegate {

	public RefreshFeaturesAction() {
		// TODO Auto-generated constructor stub
	}

	public void run(IAction action) {
		FeaturesConfigurationUtil.updateEditors(PluginUtils.getActiveShell().getDisplay(), null);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}

}
