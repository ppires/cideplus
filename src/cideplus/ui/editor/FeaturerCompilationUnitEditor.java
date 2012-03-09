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
import cideplus.ui.presentation.FeaturesPainter;

@SuppressWarnings("restriction")
public class FeaturerCompilationUnitEditor extends CompilationUnitEditor {

	private ColorPresentation colorPresentation;
	private FeaturesPainter resourceTracker = null;

	public ColorPresentation getColorPresentation() {
		return colorPresentation;
	}

	@Override
	protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {

		System.out.println("Creating java source viewer in editor inicilization.");

		ISourceViewer javaSourceViewer = super.createJavaSourceViewer(parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles, store);

		//		initAnnotationPainter((SourceViewer) javaSourceViewer);
		//
		//		if (javaSourceViewer instanceof ITextViewerExtension2) {
		//			((ITextViewerExtension2)javaSourceViewer).addPainter(annotationPainter);
		//		}

		if(javaSourceViewer instanceof ITextViewerExtension4){
			this.colorPresentation = new ColorPresentation(javaSourceViewer, this);
			//			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(colorPresentation);

			if (resourceTracker == null)
				resourceTracker = new FeaturesPainter();
			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(resourceTracker);
		}

		return javaSourceViewer;
	}

	@Override
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);
	}
}
