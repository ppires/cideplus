package cideplus.commands.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class MarkFeatureHandler extends AbstractHandler implements IHandler {

	public MarkFeatureHandler() {
		// TODO Auto-generated constructor stub
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		MessageDialog.openInformation(HandlerUtil.getActiveShellChecked(event), "My Handler", "Not yet implemented");
		return null;
	}

}
