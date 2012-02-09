package cideplus.commands.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;

import cideplus.ui.presentation.markers.FeaturesMarkerFactory;
import cideplus.utils.PluginUtils;

public class MarkFeatureHandler extends AbstractHandler implements IHandler {

	private String featureParameterId = "cideplus.commands.markFeature.featureIdParameter";

	public MarkFeatureHandler() {
		// TODO Auto-generated constructor stub
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Working!
		int featureId = Integer.parseInt(event.getParameter(featureParameterId));

		// Needs tests... Copied from FeaturesMarkerFactory.
		ITextSelection selection = PluginUtils.getCurrentTextSelection();
		try {
			IResource resource = PluginUtils.getCurrentFile();
			FeaturesMarkerFactory.createMarker(resource, selection, featureId);
		} catch (CoreException e) {
			System.out.println("Caught Exception creating marker!!!");
			e.printStackTrace();
		}

		return null;
	}

}
