package cideplus.commands.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import cideplus.FeaturerPlugin;


public class ToggleLightModeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (FeaturerPlugin.DEBUG_LIGHT_MODE)
			System.out.println("ToggleLightModeHandler.execute()");

		FeaturerPlugin.toggleLightMode();

		// must return null!
		return null;
	}
}
