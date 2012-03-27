package cideplus.commands.handlers;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import cideplus.FeaturerPlugin;
import cideplus.model.Feature;
import cideplus.model.FeaturesUtil;
import cideplus.model.ast.utils.ASTUtils;
import cideplus.model.exceptions.FeatureNotFoundException;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.ICompilationUnitFeaturesManager;
import cideplus.ui.configuration.IFeaturesManager;
import cideplus.utils.PluginUtils;

public class MarkFeatureHandler extends AbstractHandler implements IElementUpdater {

	private static final String paramFeatureId = "cideplus.commands.markFeature.featureIdParameter";
	private ITextSelection selection = null;

	public MarkFeatureHandler() {
		// TODO Auto-generated constructor stub
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service.getCommand("cideplus.commands.markFeature");
		//		State state = command.getState("cideplus.commands.markFeature.toggleState");
		//		state.setValue(!(Boolean) state.getValue());

		int featureId = Integer.parseInt(event.getParameter(paramFeatureId));

		//		ITextSelection selection = PluginUtils.getCurrentEditorTextSelection();

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
		selection = PluginUtils.getCurrentEditorTextSelection();
		int featureId = Integer.parseInt((String) parameters.get(paramFeatureId));
		ICompilationUnit compilationUnit = PluginUtils.getCurrentCompilationUnit();
		ASTNode node = ASTUtils.getNodeFromSelection(compilationUnit, selection);
		IProject project = PluginUtils.getCurrentProject();
		IFeaturesManager featuresManager = FeaturesConfigurationUtil.getFeaturesManager(project);
		ICompilationUnitFeaturesManager manager = null;
		Feature feature = null;
		try {
			manager = featuresManager.getManagerForFile(compilationUnit);
			feature = FeaturesConfigurationUtil.getFeature(project, featureId);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FeatureNotFoundException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (manager != null)
			element.setChecked(manager.hasFeature(node, feature));
	}

	//	private ICompilationUnitFeaturesManager getManagerForFile(ICompilationUnit compilationUnit) {
	//		IFeaturesManager featuresManager = FeaturesConfigurationUtil.getFeaturesManager(PluginUtils.getCurrentProject());
	//		ICompilationUnitFeaturesManager manager = null;
	//		try {
	//			manager = featuresManager.getManagerForFile(compilationUnit);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		} catch (FeatureNotFoundException e) {
	//			e.printStackTrace();
	//		} catch (CoreException e) {
	//			e.printStackTrace();
	//		}
	//		return manager;
	//	}

}
