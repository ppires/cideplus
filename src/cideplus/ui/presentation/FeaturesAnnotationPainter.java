package cideplus.ui.presentation;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import cideplus.model.Feature;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.utils.PluginUtils;

public class FeaturesAnnotationPainter extends AnnotationPainter {

	private static final String ANNOTATION_TYPE = "cideplus.ui.editor.featureAnnotation";

	public static final class FeaturesDrawingStrategy implements IDrawingStrategy {
		public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
			System.out.println("Will draw. Annotation type = " + annotation.getType());
			if (annotation instanceof FeatureAnnotation) {
				Feature feature;
				try {
					System.out.print("Drawing annotation for feature ");
					feature = FeaturesConfigurationUtil.getFeature(((FeatureAnnotation) annotation).getFeatureId(), PluginUtils.getCurrentProject());
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

	public FeaturesAnnotationPainter(ISourceViewer sourceViewer, IAnnotationAccess access) {
		super(sourceViewer, access);
	}

	//	private void setFeatures() {
	//		try {
	//			features = featuresManager.getFeatures();
	//		} catch (IOException e) {
	//			System.out.println("IOException");
	//			e.printStackTrace();
	//			throw new RuntimeException(e);
	//		} catch (CoreException e) {
	//			System.out.println("CoreException");
	//			e.printStackTrace();
	//			throw new RuntimeException(e);
	//		}
	//	}

}
