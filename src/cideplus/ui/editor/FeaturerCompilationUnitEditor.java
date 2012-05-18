package cideplus.ui.editor;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
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

	AnnotationPainter ap;

	public IVerticalRulerListener verticalRulerListener = null;

	//	public SelectAnnotationRulerAction rulerAction = null;
	//
	//	IAnnotationHover hover = null;
	//
	//	private SourceViewerConfig sourceViewerConfig = null;


	/* Hover já está funcionando sem esse source viewer config. */
	//	@Override
	//	protected JavaSourceViewerConfiguration createJavaSourceViewerConfiguration() {
	//		System.out.println("FeaturerCompilationUnitEditor.createJavaSourceViewerConfiguration()");
	//		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
	//		return new SourceViewerConfig(textTools.getColorManager(), getPreferenceStore(), this, IJavaPartitions.JAVA_PARTITIONING);
	//	}

	@Override
	public IAnnotationAccess getAnnotationAccess() {
		return super.getAnnotationAccess();
	}

	@Override
	protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		System.out.println("FeaturerCompilationUnitEditor.createJavaSourceViewer()");
		ISourceViewer javaSourceViewer = super.createJavaSourceViewer(parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles, store);

		/* Copied to EditorListener */
		/* This is here only to ensure that customAnnotationPainter is instantiated only once. */
		if (customAnnotationPainter == null) {
			customAnnotationPainter = new CustomAnnotationPainter(javaSourceViewer, getAnnotationAccess());
		}

		//		CompositeRuler c;

		/* Extension4 introduced the presentation listener concept. */
		if(javaSourceViewer instanceof ITextViewerExtension4) {
			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(customAnnotationPainter);
		}

		/* Registering IPainter */
		if(javaSourceViewer instanceof ITextViewerExtension2) {
			((ITextViewerExtension2) javaSourceViewer).addPainter(customAnnotationPainter);
		}
		/* End of copied to EditorListener */

		if (verticalRuler instanceof IVerticalRulerInfoExtension) {
			if (verticalRulerListener == null)
				verticalRulerListener = new VerticalRulerListener();
			((IVerticalRulerInfoExtension) verticalRuler).addVerticalRulerListener(verticalRulerListener);
			//			verticalRuler.getControl();
			//			IVerticalRulerColumn col;
			//			Iterator it = ((CompositeRuler) verticalRuler).getDecoratorIterator();
			//			while (it.hasNext()) {
			//				Object obj = it.next();
			//				System.out.println("object class: " + obj.getClass());
			//			}
		}

		((CompositeRuler) verticalRuler).fireAnnotationSelected(null);


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
