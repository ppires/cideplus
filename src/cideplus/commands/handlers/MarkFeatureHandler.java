package cideplus.commands.handlers;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import cideplus.FeaturerPlugin;
import cideplus.model.FeaturesUtil;
import cideplus.model.exceptions.FeatureNotFoundException;
import cideplus.utils.PluginUtils;

public class MarkFeatureHandler extends AbstractHandler implements IElementUpdater {

	private static final String paramFeatureId = "cideplus.commands.markFeature.featureIdParameter";

	public MarkFeatureHandler() {
		// TODO Auto-generated constructor stub
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		//		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		//		Command command = service.getCommand("cideplus.commands.markFeature");
		//		State state = command.getState("cideplus.commands.markFeature.toggleState");
		//		state.setValue(!(Boolean) state.getValue());

		int featureId = Integer.parseInt(event.getParameter(paramFeatureId));

		ITextSelection selection = PluginUtils.getCurrentEditorTextSelection();

		if (FeaturerPlugin.DEBUG_SELECTION)
			System.out.println("offset: " + selection.getOffset() + " / length: " + selection.getLength());

		if (selection == null || selection.isEmpty() || selection.getLength() <= 0) {
			MessageDialog.openError(PluginUtils.getActiveShell(), "CIDE+", "You must select some text in order to mark a feature.");
		}
		else {
			try {

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

			// Não é mais necessário pois as cores do texto são gerenciadas pelo CustomAnnotationPainter,
			// através das annotations.
			//			FeaturesConfigurationUtil.updateEditors(PluginUtils.getActiveShell().getDisplay(), null);
		}

		return null;
	}

	public void updateElement(UIElement element, Map parameters) {
		element.setChecked(true);

	}

}
