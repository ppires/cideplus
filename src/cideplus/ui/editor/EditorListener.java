package cideplus.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import cideplus.FeaturerPlugin;
import cideplus.ui.presentation.CustomAnnotationPainter;

public class EditorListener implements IPartListener2, IStartup {

	private Map<ISourceViewer, CustomAnnotationPainter> registeredPainters = new HashMap<ISourceViewer, CustomAnnotationPainter>();
	private Object registeredPaintersLock = new Object();

	public EditorListener() {
		super();
	}

	@Override
	public void earlyStartup() {
		final IPartListener2 thisListener = this;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window != null) {
					// updated opened editors before registering listener
					// to avoid configuring editor twice.
					updateOpenedEditors(window);
					window.getActivePage().addPartListener(thisListener);
				}
			}
		});
	}

	/**
	 * Register the painter in the newly opened editor.
	 */
	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		if (FeaturerPlugin.DEBUG_PART_LISTENER) {
			System.out.println("EditorListener.partOpened(" + partRef.getPartName() + ")");
		}

		IWorkbenchPart part = partRef.getPart(true);
		if (part != null && part instanceof IEditorPart) {
			configureJavaEditor((IEditorPart) part);
		}
	}

	/**
	 * Remove the painter when editor is closed.
	 */
	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		if (FeaturerPlugin.DEBUG_PART_LISTENER) {
			System.out.println("EditorListener.partClosed(" + partRef.getPartName() + ")");
		}

		IWorkbenchPart part = partRef.getPart(true);
		if (part != null && part instanceof IEditorPart) {
			unconfigureJavaEditor((IEditorPart) part);
		}
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) { }

	@Override
	public void partVisible(IWorkbenchPartReference partRef) { }

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) { }

	@Override
	public void partActivated(IWorkbenchPartReference partRef) { }

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) { }


	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) { }

	/**
	 * Register the painter in the editor.
	 * @param editorPart the editor to be configured
	 */
	private void configureJavaEditor(IEditorPart editorPart) {
		if (editorPart instanceof JavaEditor) {
			if (FeaturerPlugin.DEBUG_PART_LISTENER) {
				System.out.println("Configuring editor (" + editorPart.getTitle() + ")");
			}

			ISourceViewer viewer = ((JavaEditor) editorPart).getViewer();

			CustomAnnotationPainter painter = new CustomAnnotationPainter(viewer, new DefaultMarkerAnnotationAccess());

			/* Extension4 introduced the presentation listener concept. */
			if (viewer instanceof ITextViewerExtension4) {
				((ITextViewerExtension4) viewer)
				.addTextPresentationListener(painter);
			}

			/* Registering IPainter */
			if (viewer instanceof ITextViewerExtension2) {
				((ITextViewerExtension2) viewer).addPainter(painter);
			}

			/* Saving editor and painter for deactivation later */
			synchronized(registeredPaintersLock) {
				registeredPainters.put(viewer, painter);
			}
		}
	}

	/**
	 * Unregister the painter for all services he was registered.
	 * @param editorPart the editor to be unconfigured.
	 * 
	 */
	private void unconfigureJavaEditor(IEditorPart editorPart) {
		if (FeaturerPlugin.DEBUG_PART_LISTENER) {
			System.out.println("Unconfiguring editor in EditorListener");
		}

		if (editorPart instanceof JavaEditor) {
			ISourceViewer viewer = ((JavaEditor) editorPart).getViewer();
			CustomAnnotationPainter painter = null;

			synchronized(registeredPaintersLock) {
				painter = registeredPainters.get(viewer);
			}

			if (painter != null) {
				if (viewer instanceof ITextViewerExtension4) {
					((ITextViewerExtension4) viewer).removeTextPresentationListener(painter);
				}

				if (viewer instanceof ITextViewerExtension2) {
					((ITextViewerExtension2) viewer).removePainter(painter);
				}

				painter.deactivate(true);

				synchronized(registeredPaintersLock) {
					registeredPainters.remove(viewer);
				}
			}
			else
				if (FeaturerPlugin.DEBUG_PAINTER_MNGR) {
					System.out.println("  - painter is null (probably wasn't in map)...");
				}
		}
	}

	/**
	 * Updates all java editors opened when eclipse starts, registering the
	 * the painter.
	 * @param window the active workbench window.
	 */
	private void updateOpenedEditors(IWorkbenchWindow window) {
		if (FeaturerPlugin.DEBUG_PART_LISTENER) {
			System.out.println("Updating opened editors...");
		}

		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			IEditorReference editors[] = page.getEditorReferences();
			for (IEditorReference iEditorReference : editors) {
				IEditorPart editor = iEditorReference.getEditor(true);
				configureJavaEditor(editor);
			}
		} else {
			if (FeaturerPlugin.DEBUG_PART_LISTENER) {
				System.out.println("  - active page is null...");
			}
		}
	}

}
