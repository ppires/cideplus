package cideplus.ui.presentation;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import cideplus.model.Feature;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.utils.PluginUtils;

public class FeaturesDrawingStrategy implements IDrawingStrategy {

	public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
		System.out.println("Will draw. Annotation type = " + annotation.getType());
		if (annotation instanceof FeatureAnnotation) {
			Feature feature;
			try {
				System.out.print("Drawing annotation for feature ");
				feature = FeaturesConfigurationUtil.getFeature(PluginUtils.getCurrentProject(), ((FeatureAnnotation) annotation).getFeatureId());
				System.out.println(feature.getName());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
