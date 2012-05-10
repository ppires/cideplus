package cideplus.ui.editor;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import cideplus.FeaturerPlugin;
import cideplus.ui.presentation.CustomAnnotationPainter;

public class EditorListener implements IPartListener2 {

	private CustomAnnotationPainter customAnnotationPainter = null;

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		System.out.println("EditorListener.partActivated()");
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		System.out.println("EditorListener.partBroughtToTop()");
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		System.out.println("EditorListener.partClosed()");
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		System.out.println("EditorListener.partDeactivated()");
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(true);
		if (part != null) {
			if (part instanceof CompilationUnitEditor) {

				ISourceViewer sourceViewer = ((CompilationUnitEditor) part).getViewer();
				configureSourceViewer(sourceViewer);

			}
			else {
				if (FeaturerPlugin.DEBUG_PART_LISTENER)
					System.out.println("part NOT instanceof CompilationUnitEditor");
			}
		}
		else {
			if (FeaturerPlugin.DEBUG_PART_LISTENER)
				System.out.println("PartListener: part == null");
		}
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		System.out.println("EditorListener.partHidden()");
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		System.out.println("EditorListener.partVisible()");
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		System.out.println("EditorListener.partInputChanged()");
	}

	private void configureSourceViewer(ISourceViewer sourceViewer) {
		if (customAnnotationPainter == null) {
			initializePainter(sourceViewer);
		}
	}

	private void initializePainter(ISourceViewer sourceViewer) {
		customAnnotationPainter = new CustomAnnotationPainter(sourceViewer, new DefaultMarkerAnnotationAccess());

		/* Extension4 introduced the presentation listener concept. */
		if(sourceViewer instanceof ITextViewerExtension4) {
			((ITextViewerExtension4)sourceViewer).addTextPresentationListener(customAnnotationPainter);
		}

		/* Registering IPainter */
		if(sourceViewer instanceof ITextViewerExtension2) {
			((ITextViewerExtension2) sourceViewer).addPainter(customAnnotationPainter);
		}

	}

	private void deactivatePainter(ISourceViewer sourceViewer) {
		if (sourceViewer != null) {
			if (sourceViewer instanceof ITextViewerExtension4)
				((ITextViewerExtension4) sourceViewer).removeTextPresentationListener(customAnnotationPainter);
			StyledText textWidget = sourceViewer.getTextWidget();
			if (textWidget != null) {
				textWidget.removePaintListener(customAnnotationPainter);
				customAnnotationPainter.deactivate(false);
			}
		}
	}

}
