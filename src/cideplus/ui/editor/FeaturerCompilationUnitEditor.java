package cideplus.ui.editor;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

import cideplus.FeaturerPlugin;
import cideplus.ui.presentation.ColorPresentation;
import cideplus.ui.presentation.CustomAnnotationPainter;
import cideplus.ui.presentation.FeatureAnnotation;

@SuppressWarnings("restriction")
public class FeaturerCompilationUnitEditor extends CompilationUnitEditor {

	private ColorPresentation colorPresentation;

	private CustomAnnotationPainter customAnnotationPainter = null;
	//	private FeaturesPainter featuresPainter = null;
	//	private FeaturesAnnotationHover annotationHover = null;



	/**
	 * Returns the color presentation object used by this editor.
	 * 
	 * @return The color presentation used by this editor.
	 */
	public ColorPresentation getColorPresentation() {
		return colorPresentation;
	}


	@Override
	protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		ISourceViewer javaSourceViewer = super.createJavaSourceViewer(parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles, store);

		/* No longer used. Based on AST parsing to color the code. Too inefficient. */
		//this.colorPresentation = new ColorPresentation(javaSourceViewer, this);

		/* No longer used too. Efficient, but only updates text presentation when the file is saved. */
		//		if (featuresPainter == null)
		//			featuresPainter = new FeaturesPainter(javaSourceViewer);

		/* Completely copied from org.eclipse.jface.text.source.AnnotationPainter */
		if (customAnnotationPainter == null) {
			customAnnotationPainter = new CustomAnnotationPainter(javaSourceViewer, fAnnotationAccess);
			customAnnotationPainter.addAnnotationType(FeatureAnnotation.TYPE, null);// null because highlighting strategy is hardcoded!
		}


		//		if (annotationHover == null)
		//			annotationHover = new FeaturesAnnotationHover();
		//		javaSourceViewer.setVerticalRulerAnnotationHover();
		//		javaSourceViewer.setAnnotationHover(annotationHover);

		/* Extension4 introduced the presentation listener concept. */
		if(javaSourceViewer instanceof ITextViewerExtension4) {
			//			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(colorPresentation);
			//			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(featuresPainter);
			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(customAnnotationPainter);
			if (FeaturerPlugin.DEBUG_PRESENTATION)
				System.out.println("Registered text presentation listener!!");
		}

		/* Registering IPainter */
		if(javaSourceViewer instanceof ITextViewerExtension2) {
			((ITextViewerExtension2) javaSourceViewer).addPainter(customAnnotationPainter);
			if (FeaturerPlugin.DEBUG_PRESENTATION)
				System.out.println("Registered painter!");
		}


		return javaSourceViewer;
	}

	//	@Override
	//	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
	//		super.configureSourceViewerDecorationSupport(support);
	//	}

	@Override
	public void dispose() {
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer != null) {
			StyledText textWidget = sourceViewer.getTextWidget();
			if (textWidget != null) {
				customAnnotationPainter.deactivate(false);
				textWidget.removePaintListener(customAnnotationPainter);
			}
		}
		super.dispose();
	}

	//	public void setAnnotationModel() {
	//		if (FeaturerPlugin.DEBUG_PRESENTATION)
	//			System.out.println("Setting editor annotation model.");
	//
	//		ISourceViewer sourceViewer = getSourceViewer();
	//		if (sourceViewer != null) {
	//			IAnnotationModel model = sourceViewer.getAnnotationModel();
	//			if (model != null) {
	//				model.addAnnotationModelListener(customAnnotationPainter);
	//				if (FeaturerPlugin.DEBUG_PRESENTATION)
	//					System.out.println("Registered annotation model listener!");
	//			}
	//			else
	//				if (FeaturerPlugin.DEBUG_PRESENTATION)
	//					System.out.println("Failed to register annotation model listener: model == null");
	//		}
	//		else
	//			if (FeaturerPlugin.DEBUG_PRESENTATION)
	//				System.out.println("Failed to register annotation model listener: sourceViewer == null");
	//	}
}
