package cideplus.ui.presentation;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import cideplus.FeaturerPlugin;

public class FeatureAnnotation extends MarkerAnnotation {

	public static final String TYPE = "cideplus.ui.editor.featureAnnotation";
	private int featureId;

	public FeatureAnnotation(IMarker marker) {
		super(marker);
		this.featureId = Integer.parseInt(marker.getAttribute("featureId", "0"));

		if (FeaturerPlugin.DEBUG_PRESENTATION)
			System.out.println("Constructor from FeatureAnnotation");
	}

	public int getFeatureId() {
		return featureId;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	public Color getColor() {
		if (FeaturerPlugin.DEBUG_PRESENTATION)
			System.out.println("FeatureAnnotation.getColor()");
		return new Color(Display.getDefault(), 255, 0, 0);
	}

}
