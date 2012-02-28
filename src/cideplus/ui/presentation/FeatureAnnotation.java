package cideplus.ui.presentation;

import org.eclipse.jface.text.source.Annotation;

public class FeatureAnnotation extends Annotation {

	public static final String TYPE = "cideplus.ui.editor.featureAnnotation";
	private int featureId;

	public FeatureAnnotation(int featureId) {
		super();
		this.featureId = featureId;
	}

	public int getFeatureId() {
		return featureId;
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
