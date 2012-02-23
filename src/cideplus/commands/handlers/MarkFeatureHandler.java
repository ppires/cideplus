package cideplus.commands.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;

import cideplus.model.FeaturesUtil;
import cideplus.model.exceptions.FeatureNotFoundException;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.utils.PluginUtils;

public class MarkFeatureHandler extends AbstractHandler implements IHandler {

	private static final String paramFeatureId = "cideplus.commands.markFeature.featureIdParameter";

	public MarkFeatureHandler() {
		// TODO Auto-generated constructor stub
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		int featureId = Integer.parseInt(event.getParameter(paramFeatureId));

		ITextSelection selection = PluginUtils.getCurrentEditorTextSelection();
		if (selection == null || selection.isEmpty()) {
			MessageDialog.openError(PluginUtils.getActiveShell(), "CIDE+", "You must select some text in order to mark a feature.");
		}
		else {
			try {

				/* Mark feature */
				FeaturesUtil.markFeature(featureId, selection.getOffset(), selection.getLength());

			} catch (FeatureNotFoundException e) {
				MessageDialog.openError(PluginUtils.getActiveShell(), "Feature Not Found", e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (CoreException e) {
				MessageDialog.openError(PluginUtils.getActiveShell(), "Could Not Mark Feature", e.getMessage());
				e.printStackTrace();
			}

			/* Refresh the editor */
			FeaturesConfigurationUtil.updateEditors(PluginUtils.getActiveShell().getDisplay(), null);
		}

		//FeaturesMarkerFactory.printAllMarkers();

		return null;
	}

}
