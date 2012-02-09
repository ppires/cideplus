package cideplus.ui.editor.popup.actions;

import java.util.HashMap;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

public class MenuContentProvider extends CompoundContributionItem {

	private static int counter = 0;

	//	private static Set<Feature> features;
	private static String featureIdParameter = "cideplus.commands.markFeature.featureIdParameter";

	public MenuContentProvider() {
		// TODO Auto-generated constructor stub
	}

	public MenuContentProvider(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IContributionItem[] getContributionItems() {
		IServiceLocator serviceLocator = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		String id = "cideplus.commands.markFeature.commandParameterId";
		String commandId = "cideplus.commands.markFeature";

		final CommandContributionItemParameter contributionParameter =
				new CommandContributionItemParameter(serviceLocator, id, commandId, SWT.NONE);
		contributionParameter.label = "Dynamic Menu Item " + counter++;

		Object key = "cideplus.commands.markFeature.featureIdParameter";
		Object value = "123";
		contributionParameter.parameters = new HashMap();
		contributionParameter.parameters.put(key, value);

		return new IContributionItem[] {
				new CommandContributionItem(contributionParameter) };
	}

}
