package cideplus.ui.editor;

import java.util.Iterator;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfoExtension;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

import cideplus.ui.presentation.CustomAnnotationPainter;
import cideplus.ui.presentation.VerticalRulerListener;

@SuppressWarnings("restriction")
public class FeaturerCompilationUnitEditor extends CompilationUnitEditor {

	private CustomAnnotationPainter customAnnotationPainter = null;
	//	private FeaturesPainter featuresPainter = null;
	//	private FeaturesAnnotationHover annotationHover = null;

	public IVerticalRulerListener verticalRulerListener = null;


	@Override
	protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		ISourceViewer javaSourceViewer = super.createJavaSourceViewer(parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles, store);

		//		CompositeRuler ruler;
		AnnotationRulerColumn col;


		System.out.println("Vertical ruler class: " + verticalRuler.getClass());
		if (verticalRuler instanceof IVerticalRulerInfoExtension) {
			if (verticalRulerListener == null)
				verticalRulerListener = new VerticalRulerListener();
			((IVerticalRulerInfoExtension) verticalRuler).addVerticalRulerListener(verticalRulerListener);
			//			verticalRuler.getControl();
			//			IVerticalRulerColumn col;
			Iterator it = ((CompositeRuler) verticalRuler).getDecoratorIterator();
			while (it.hasNext()) {
				Object obj = it.next();
				System.out.println("object class: " + obj.getClass());
			}
		}

		/* No longer used too. Efficient, but only updates text presentation when the file is saved. */
		//		if (featuresPainter == null)
		//			featuresPainter = new FeaturesPainter(javaSourceViewer);

		/* Completely copied from org.eclipse.jface.text.source.AnnotationPainter */
		if (customAnnotationPainter == null)
			customAnnotationPainter = new CustomAnnotationPainter(javaSourceViewer, fAnnotationAccess);




		//		if (annotationHover == null)
		//			annotationHover = new FeaturesAnnotationHover();
		//		javaSourceViewer.setVerticalRulerAnnotationHover();
		//		javaSourceViewer.setAnnotationHover(annotationHover);

		/* Extension4 introduced the presentation listener concept. */
		if(javaSourceViewer instanceof ITextViewerExtension4) {
			//			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(colorPresentation);
			//			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(featuresPainter);
			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(customAnnotationPainter);
		}

		/* Registering IPainter */
		if(javaSourceViewer instanceof ITextViewerExtension2) {
			((ITextViewerExtension2) javaSourceViewer).addPainter(customAnnotationPainter);
			//			((ITextViewerExtension2) javaSourceViewer).addPainter(featuresPainter);
		}

		return javaSourceViewer;
	}

	@Override
	public void dispose() {
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer != null) {
			if (sourceViewer instanceof ITextViewerExtension4)
				((ITextViewerExtension4) sourceViewer).removeTextPresentationListener(customAnnotationPainter);
			StyledText textWidget = sourceViewer.getTextWidget();
			if (textWidget != null) {
				textWidget.removePaintListener(customAnnotationPainter);
				customAnnotationPainter.deactivate(false);
			}
		}
		super.dispose();
	}

}
