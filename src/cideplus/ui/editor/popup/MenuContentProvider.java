package cideplus.ui.editor.popup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import cideplus.model.Feature;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.IFeaturesManager;
import cideplus.utils.PluginUtils;

public class MenuContentProvider extends CompoundContributionItem {

	private Set<Feature> features;

	private static String paramFeatureId = "cideplus.commands.markFeature.featureIdParameter";

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
		String markFeatureCommandId = "cideplus.commands.markFeature";
		String configureFeaturesCommandId = "cideplus.commands.configureFeatures";

		if (features.isEmpty()) {
			final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(serviceLocator, id, configureFeaturesCommandId, SWT.NONE);
			contributionParameter.label = "Configure Features...";

			return new IContributionItem[] { new CommandContributionItem(contributionParameter) };
		}
		else {
			IContributionItem[] menuItems = new IContributionItem[features.size()];
			Iterator<Feature> it = features.iterator();
			for (int i = 0; it.hasNext(); i++) {
				Feature feature = it.next();
				final CommandContributionItemParameter contributionParameter = new CommandContributionItemParameter(serviceLocator, id, markFeatureCommandId, CommandContributionItem.STYLE_PULLDOWN);
				contributionParameter.label = feature.getName();
				contributionParameter.parameters = new HashMap();
				contributionParameter.parameters.put(paramFeatureId, feature.getId().toString());
				menuItems[i] = new CommandContributionItem(contributionParameter);
			}
			return menuItems;
		}
	}

	private void setFeatures() {
		IFeaturesManager featuresManager = FeaturesConfigurationUtil.getFeaturesManager(PluginUtils.getCurrentProject());
		try {
			features = featuresManager.getFeatures();
		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (CoreException e) {
			System.out.println("CoreException");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}