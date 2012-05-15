package cideplus.ui.editor;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import cideplus.FeaturerPlugin;
import cideplus.ui.presentation.CustomAnnotationPainter;

public class EditorListener implements IPartListener2 {

	//	private CustomAnnotationPainter customAnnotationPainter = null;

	public EditorListener() {
		super();
		updateOpenedEditors();
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		if (FeaturerPlugin.DEBUG_PART_LISTENER)
			System.out.println("EditorListener.partActivated(" + partRef.getPartName() + ")");
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		if (FeaturerPlugin.DEBUG_PART_LISTENER)
			System.out.println("EditorListener.partBroughtToTop(" + partRef.getPartName() + ")");
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		if (FeaturerPlugin.DEBUG_PART_LISTENER)
			System.out.println("EditorListener.partClosed(" + partRef.getPartName() + ")");
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		if (FeaturerPlugin.DEBUG_PART_LISTENER)
			System.out.println("EditorListener.partDeactivated(" + partRef.getPartName() + ")");
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		if (FeaturerPlugin.DEBUG_PART_LISTENER)
			System.out.println("EditorListener.partOpened(" + partRef.getPartName() + ")");

		IWorkbenchPart part = partRef.getPart(true);
		if (part != null) {
			if (part instanceof CompilationUnitEditor) {

				ISourceViewer sourceViewer = ((CompilationUnitEditor) part).getViewer();
				if (sourceViewer != null) {
					configureSourceViewer(sourceViewer);
				}
				else {
					System.out.println("sourceViewer is null");
				}

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
		if (FeaturerPlugin.DEBUG_PART_LISTENER)
			System.out.println("EditorListener.partHidden(" + partRef.getPartName() + ")");
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		if (FeaturerPlugin.DEBUG_PART_LISTENER)
			System.out.println("EditorListener.partVisible(" + partRef.getPartName() + ")");
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		if (FeaturerPlugin.DEBUG_PART_LISTENER)
			System.out.println("EditorListener.partInputChanged(" + partRef.getPartName() + ")");
	}





	private void configureSourceViewer(ISourceViewer sourceViewer) {
		//		if (customAnnotationPainter == null) {
		//			customAnnotationPainter = new CustomAnnotationPainter(sourceViewer, new DefaultMarkerAnnotationAccess());
		//		}
		/* Extension4 introduced the presentation listener concept. */
		if(sourceViewer instanceof ITextViewerExtension4) {
			((ITextViewerExtension4)sourceViewer).addTextPresentationListener(new CustomAnnotationPainter(sourceViewer, new DefaultMarkerAnnotationAccess()));
		}

		/* Registering IPainter */
		if(sourceViewer instanceof ITextViewerExtension2) {
			((ITextViewerExtension2) sourceViewer).addPainter(new CustomAnnotationPainter(sourceViewer, new DefaultMarkerAnnotationAccess()));
		}
	}

	//	private void deactivatePainter(ISourceViewer sourceViewer) {
	//		if (sourceViewer != null) {
	//			if (sourceViewer instanceof ITextViewerExtension4)
	//				((ITextViewerExtension4) sourceViewer).removeTextPresentationListener(customAnnotationPainter);
	//			StyledText textWidget = sourceViewer.getTextWidget();
	//			if (textWidget != null) {
	//				textWidget.removePaintListener(customAnnotationPainter);
	//				customAnnotationPainter.deactivate(false);
	//			}
	//		}
	//	}

	private void updateOpenedEditors() {
		System.out.println("Updating opened editors...");
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if (window != null) {

				IWorkbenchPage[] pages = window.getPages();
				for (IWorkbenchPage page : pages) {
					IEditorPart editor = page.getActiveEditor();
					if (editor instanceof CompilationUnitEditor) {
						configureSourceViewer(((CompilationUnitEditor) editor).getViewer());
					}
				}

			}
			else {
				if (FeaturerPlugin.DEBUG_PART_LISTENER)
					System.out.println("workbench window is null (no active workbench window or called from outside UI thread...)");
			}
		}
		else {
			if (FeaturerPlugin.DEBUG_PART_LISTENER)
				System.out.println("workbench == null");
		}
	}

}
