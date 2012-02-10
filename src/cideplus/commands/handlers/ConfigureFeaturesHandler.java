package cideplus.commands.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.dialogs.ConfigureFeaturesDialog;
import cideplus.utils.PluginUtils;

public class ConfigureFeaturesHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		Shell shell = PluginUtils.getActiveShell();
		IJavaProject project = PluginUtils.getCurrentJavaProject();

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

		return null;
	}

}
