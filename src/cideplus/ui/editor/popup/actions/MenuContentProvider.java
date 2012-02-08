package cideplus.ui.editor.popup.actions;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

public class MenuContentProvider extends CompoundContributionItem {

	private static int counter = 0;

	public MenuContentProvider() {
		// TODO Auto-generated constructor stub
	}

	public MenuContentProvider(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		final CommandContributionItemParameter contributionParameter =
				new CommandContributionItemParameter(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
						"my.project.myCommandContributionItem", "cideplus.commands.markFeature",
						SWT.NONE);
		contributionParameter.label = "Dynamic Menu Item " + counter++;
		return new IContributionItem[] {
				new CommandContributionItem(contributionParameter) };
	}

}
