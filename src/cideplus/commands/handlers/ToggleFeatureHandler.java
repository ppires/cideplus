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
import org.eclipse.ui.texteditor.ITextEditor;

import cideplus.model.FeaturesUtil;
import cideplus.model.exceptions.FeatureNotFoundException;
import cideplus.ui.editor.popup.MenuContentProvider;
import cideplus.utils.PluginUtils;

public class ToggleFeatureHandler extends AbstractHandler implements IElementUpdater {

	public ToggleFeatureHandler() {

		/* Testing code... This constructor should be empty! */
		//		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		//		if (window != null) {
		//			IWorkbenchPage page = window.getActivePage();
		//			if (page != null) {
		//				IEditorPart editor = page.getActiveEditor();
		//				if (editor instanceof ITextEditor) {
		//					Object adapted = editor.getAdapter(IVerticalRuler.class);
		//					if (adapted instanceof IVerticalRulerInfoExtension) {
		//						((IVerticalRulerInfoExtension)adapted).addVerticalRulerListener(new VerticalRulerListener());
		//					}
		//				}
		//			}
		//		}

	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		/* Testando */
		//		ITextEditor editor = PluginUtils.getCurrentTextEditor();
		//		System.out.println();
		ITextEditor editor = PluginUtils.getCurrentTextEditor();
		System.out.println("editor class: " + editor.getClass());


		int featureId = Integer.parseInt(event.getParameter(MenuContentProvider.paramFeatureId));
		Boolean checked = new Boolean(event.getParameter(MenuContentProvider.paramChecked));
		ITextSelection selection = PluginUtils.getCurrentEditorTextSelection();

		if (selection == null || selection.isEmpty() || selection.getLength() <= 0) {
			MessageDialog.openError(PluginUtils.getActiveShell(), "CIDE+", "You must select some text in order to mark a feature.");
		}
		else {
			try {

				if (checked)
					FeaturesUtil.unmarkFeature(featureId, selection.getOffset(), selection.getLength());
				else
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
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	public void updateElement(UIElement element, Map parameters) {
		Boolean checked = new Boolean((String) parameters.get(MenuContentProvider.paramChecked));
		element.setChecked(checked);
	}
}
