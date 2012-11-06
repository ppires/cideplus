package cideplus.ui.editor.popup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import cideplus.model.Feature;
import cideplus.model.ast.utils.ASTUtils;
import cideplus.model.exceptions.FeatureNotFoundException;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.ICompilationUnitFeaturesManager;
import cideplus.ui.configuration.IFeaturesManager;
import cideplus.utils.PluginUtils;

public class MenuContentProvider extends CompoundContributionItem {

	private ICompilationUnitFeaturesManager managerForFile;
	private Set<Feature> features;

	public static final String paramFeatureId = "cideplus.commands.markFeature.featureIdParameter";
	public static final String paramChecked = "cideplus.commands.markFeature.checkedParameter";

	private static final String markFeatureCommandId = "cideplus.commands.markFeature";
	private static final String configureFeaturesCommandId = "cideplus.commands.configureFeatures";

	public MenuContentProvider() {
		setFeatures();
	}

	public MenuContentProvider(String id) {
		super(id);
		setFeatures();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected IContributionItem[] getContributionItems() {
		IServiceLocator serviceLocator = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		String id = "cideplus.commands.markFeature.commandParameterId";

		if (features.isEmpty()) {
			final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(serviceLocator, id, configureFeaturesCommandId, SWT.NONE);
			contributionParameter.label = "Configure Features...";

			return new IContributionItem[] { new CommandContributionItem(contributionParameter) };
		}
		else {
			IContributionItem[] menuItems = new IContributionItem[features.size() + 1];
			ITextSelection selection = PluginUtils.getCurrentEditorTextSelection();
			ASTNode node = ASTUtils.getNodeFromSelection(managerForFile.getCompilationUnit(), selection);

			//			System.out.println("Selection:\n  offset: " + selection.getOffset() + "\n  length: " + selection.getLength());
			//			System.out.println("AST Node:\n  offset: " + node.getStartPosition() + "\n  length: " + node.getLength());

			Iterator<Feature> it = features.iterator();
			int i;
			for (i = 0; it.hasNext(); i++) {
				Feature feature = it.next();
				Boolean checked = managerForFile.hasFeature(node, feature);
				final CommandContributionItemParameter item = new CommandContributionItemParameter(serviceLocator, id, markFeatureCommandId, CommandContributionItem.STYLE_CHECK);
				item.label = feature.getName();
				item.parameters = new HashMap();
				item.parameters.put(paramFeatureId, feature.getId().toString());
				item.parameters.put(paramChecked, checked.toString());
				menuItems[i] = new CommandContributionItem(item);
			}
			menuItems[i++] = new Separator();
			return menuItems;
		}
	}

	private void setFeatures() {
		IFeaturesManager featuresManager = FeaturesConfigurationUtil.getFeaturesManager(PluginUtils.getCurrentProject());
		try {
			features = featuresManager.getFeatures();
			managerForFile = featuresManager.getManagerForFile(PluginUtils.getCurrentCompilationUnit());
		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (CoreException e) {
			System.out.println("CoreException");
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (FeatureNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
