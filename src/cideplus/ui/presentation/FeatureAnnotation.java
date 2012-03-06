package cideplus.ui.presentation;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import cideplus.FeaturerPlugin;

public class FeatureAnnotation extends Annotation {

	public static final String TYPE = "cideplus.ui.editor.featureAnnotation";
	private IMarker marker;
	private int featureId;

	public FeatureAnnotation(IMarker marker) {
		super();
		this.marker = marker;
		this.featureId = Integer.parseInt(marker.getAttribute("feature_id", "0"));

		if (FeaturerPlugin.DEBUG_PRESENTATION)
			System.out.println("Constructor from FeatureAnnotation");
	}

	public int getFeatureId() {
		return featureId;
	}

	public IMarker getMarker() {
		return marker;
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
