package cideplus.ui.editor;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import cideplus.ui.presentation.ColorPresentation;
import cideplus.ui.presentation.ResourceChangeListener;

@SuppressWarnings("restriction")
public class FeaturerCompilationUnitEditor extends CompilationUnitEditor {

	private ColorPresentation colorPresentation;

	public ColorPresentation getColorPresentation() {
		return colorPresentation;
	}

	@Override
	protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		ISourceViewer javaSourceViewer = super.createJavaSourceViewer(parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles, store);

		//		initAnnotationPainter((SourceViewer) javaSourceViewer);
		//
		//		if (javaSourceViewer instanceof ITextViewerExtension2) {
		//			((ITextViewerExtension2)javaSourceViewer).addPainter(annotationPainter);
		//		}

		if(javaSourceViewer instanceof ITextViewerExtension4){
			this.colorPresentation = new ColorPresentation(javaSourceViewer, this);
			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(colorPresentation);
			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(new ResourceChangeListener());
		}

		return javaSourceViewer;
	}

	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);
	}

	//	private void initAnnotationPainter(SourceViewer sourceViewer) {
	//		IAnnotationAccess annotationAccess = new IAnnotationAccess() {
	//
	//			public Object getType(Annotation annotation) {
	//				return annotation.getType();
	//			}
	//
	//			public boolean isMultiLine(Annotation annotation) {
	//				return true;
	//			}
	//
	//			public boolean isTemporary(Annotation annotation) {
	//				return true;
	//			}
	//
	//		};
	//
	//		annotationPainter = new FeaturesAnnotationPainter(sourceViewer, annotationAccess);
	//		annotationPainter.addAnnotationType(FeatureAnnotation.TYPE, FeatureAnnotation.TYPE);
	//		annotationPainter.addTextStyleStrategy(FeatureAnnotation.TYPE, new AnnotationPainter.HighlightingStrategy());
	//		//		annotationPainter.setAnnotationTypeColor(FeatureAnnotation.TYPE, getTextWidget().getForeground());
	//	}
}
