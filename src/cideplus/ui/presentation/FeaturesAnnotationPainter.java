package cideplus.ui.presentation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
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

	/**
	 * Fields copied from AnnotationPainter to make
	 * applyTextPresentation() work.
	 */
	private Object fHighlightedDecorationsMapLock= new Object();
	private Map fHighlightedDecorationsMap= new HashMap();
	private ISourceViewer fSourceViewer;

	/**
	 * The presentation information (decoration) for an annotation.  Each such
	 * object represents one decoration drawn on the text area, such as squiggly lines
	 * and underlines.
	 */
	private static class Decoration {
		/** The position of this decoration */
		private Position fPosition;
		/** The color of this decoration */
		private Color fColor;
		/**
		 * The annotation's layer
		 * @since 3.0
		 */
		private int fLayer;
		/**
		 * The painting strategy for this decoration.
		 * @since 3.0
		 */
		private Object fPaintingStrategy;
	}




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
		System.out.println("Painter Constructor!");
		fSourceViewer = sourceViewer;
	}



	/*
	 * Completely copied from AnnotationPainter.
	 */
	@Override
	public void applyTextPresentation(TextPresentation tp) {
		//		if (FeaturerPlugin.DEBUG_ANNOTATIONS)
		//			System.out.println("Applying text presentation from painter!");
		//
		//		Set decorations;
		//
		//		synchronized (fHighlightedDecorationsMapLock) {
		//			if (fHighlightedDecorationsMap == null || fHighlightedDecorationsMap.isEmpty())
		//				return;
		//
		//			decorations= new HashSet(fHighlightedDecorationsMap.entrySet());
		//		}
		//
		//		IRegion region= tp.getExtent();
		//
		//		if (FeaturerPlugin.DEBUG_ANNOTATIONS)
		//			System.out.println("AP: applying text presentation offset: " + region.getOffset() + ", length= " + region.getLength()); //$NON-NLS-1$ //$NON-NLS-2$
		//
		//		for (int layer= 0, maxLayer= 1;	layer < maxLayer; layer++) {
		//
		//			for (Iterator iter= decorations.iterator(); iter.hasNext();) {
		//				Map.Entry entry= (Map.Entry)iter.next();
		//
		//				Annotation a= (Annotation)entry.getKey();
		//				if (a.isMarkedDeleted())
		//					continue;
		//
		//				Decoration pp = (Decoration)entry.getValue();
		//
		//				maxLayer= Math.max(maxLayer, pp.fLayer + 1); // dynamically update layer maximum
		//				if (pp.fLayer != layer)	// wrong layer: skip annotation
		//					continue;
		//
		//				Position p= pp.fPosition;
		//				if (fSourceViewer instanceof ITextViewerExtension5) {
		//					ITextViewerExtension5 extension3= (ITextViewerExtension5) fSourceViewer;
		//					if (null == extension3.modelRange2WidgetRange(new Region(p.getOffset(), p.getLength())))
		//						continue;
		//				} else if (!fSourceViewer.overlapsWithVisibleRegion(p.offset, p.length)) {
		//					continue;
		//				}
		//
		//				int regionEnd= region.getOffset() + region.getLength();
		//				int pEnd= p.getOffset() + p.getLength();
		//				if (pEnd >= region.getOffset() && regionEnd > p.getOffset()) {
		//					int start= Math.max(p.getOffset(), region.getOffset());
		//					int end= Math.min(regionEnd, pEnd);
		//					int length= Math.max(end - start, 0);
		//					StyleRange styleRange= new StyleRange(start, length, null, null);
		//					if (a.getType() == FeatureAnnotation.TYPE)
		//						((ITextStyleStrategy)pp.fPaintingStrategy).applyTextStyle(styleRange, ((FeatureAnnotation) a).getColor());
		//					else
		//						((ITextStyleStrategy)pp.fPaintingStrategy).applyTextStyle(styleRange, pp.fColor);
		//
		//					tp.mergeStyleRange(styleRange);
		//				}
		//			}
		//		}
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
