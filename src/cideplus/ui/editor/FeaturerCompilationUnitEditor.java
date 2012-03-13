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
import cideplus.ui.presentation.FeaturesAnnotationHover;
import cideplus.ui.presentation.FeaturesPainter;

@SuppressWarnings("restriction")
public class FeaturerCompilationUnitEditor extends CompilationUnitEditor {

	private ColorPresentation colorPresentation;
	public ColorPresentation getColorPresentation() {
		return colorPresentation;
	}



	private FeaturesPainter featuresPainter = null;
	private FeaturesAnnotationHover annotationHover = null;

	@Override
	protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {

		ISourceViewer javaSourceViewer = super.createJavaSourceViewer(parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles, store);

		if(javaSourceViewer instanceof ITextViewerExtension4){
			//			this.colorPresentation = new ColorPresentation(javaSourceViewer, this);
			//			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(colorPresentation);

			if (featuresPainter == null)
				featuresPainter = new FeaturesPainter();
			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(featuresPainter);
		}

		if (annotationHover == null)
			annotationHover = new FeaturesAnnotationHover();
		javaSourceViewer.setAnnotationHover(annotationHover);

		return javaSourceViewer;
	}

	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);
	}
}
