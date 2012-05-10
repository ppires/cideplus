package cideplus.ui.editor;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfoExtension;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.swt.widgets.Composite;

import cideplus.ui.presentation.VerticalRulerListener;

@SuppressWarnings("restriction")
public class FeaturerCompilationUnitEditor extends CompilationUnitEditor {

	//	private CustomAnnotationPainter customAnnotationPainter = null;

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
	protected ISourceViewer createJavaSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean isOverviewRulerVisible, int styles, IPreferenceStore store) {
		ISourceViewer javaSourceViewer = super.createJavaSourceViewer(parent, verticalRuler, overviewRuler, isOverviewRulerVisible, styles, store);

		/* Copied to EditorListener */
		//		/* This is here only to ensure that customAnnotationPainter is instantiated only once. */
		//		if (customAnnotationPainter == null) {
		//			customAnnotationPainter = new CustomAnnotationPainter(javaSourceViewer, getAnnotationAccess());
		//		}
		//
		//		/* Extension4 introduced the presentation listener concept. */
		//		if(javaSourceViewer instanceof ITextViewerExtension4) {
		//			((ITextViewerExtension4)javaSourceViewer).addTextPresentationListener(customAnnotationPainter);
		//		}
		//
		//		/* Registering IPainter */
		//		if(javaSourceViewer instanceof ITextViewerExtension2) {
		//			((ITextViewerExtension2) javaSourceViewer).addPainter(customAnnotationPainter);
		//		}
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


		//		CompositeRuler ruler;
		//		AnnotationRulerColumn col;
		//		System.out.println("Vertical ruler class: " + verticalRuler.getClass());






		//		if (hover == null)
		//			hover = new FeaturesAnnotationHover();
		//		((Object) javaSourceViewer).setVerticalRulerAnnotationHover();
		//		javaSourceViewer.setAnnotationHover(hover);

		//		if (verticalRulerListener == null) {
		//			verticalRulerListener = new VerticalRulerListener();
		//			((CompositeRuler) verticalRuler).
		//		}

		//		if (rulerAction == null) {
		//			rulerAction = new SelectAnnotationRulerAction(null, "cideplus", this);
		//		}
		//		else  {
		//			System.out.println("rulerAction already exists!");
		//		}

		//		javaSourceViewer.

		//		System.out.println("javaSourceViewer class: " + javaSourceViewer.getClass());

		//		javaSourceViewer.setAnnotationHover(hover);
		//		((SourceViewer) javaSourceViewer).showAnnotations(true);



		return javaSourceViewer;
	}

	//	@Override
	//	public void dispose() {
	//		ISourceViewer sourceViewer = getSourceViewer();
	//		if (sourceViewer != null) {
	//			if (sourceViewer instanceof ITextViewerExtension4)
	//				((ITextViewerExtension4) sourceViewer).removeTextPresentationListener(customAnnotationPainter);
	//			StyledText textWidget = sourceViewer.getTextWidget();
	//			if (textWidget != null) {
	//				textWidget.removePaintListener(customAnnotationPainter);
	//				customAnnotationPainter.deactivate(false);
	//			}
	//		}
	//		super.dispose();
	//	}

}
