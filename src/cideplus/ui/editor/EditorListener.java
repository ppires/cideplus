package cideplus.ui.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerInfoExtension;
import org.eclipse.swt.widgets.Control;
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
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import cideplus.FeaturerPlugin;
import cideplus.ui.presentation.CustomAnnotationPainter;
import cideplus.ui.presentation.RulerMouseListener;
import cideplus.ui.presentation.VerticalRulerListener;

public class EditorListener implements IPartListener2, IStartup {

	private static Map<ISourceViewer, CustomAnnotationPainter> registeredPainters = new HashMap<ISourceViewer, CustomAnnotationPainter>();
	private static Object registeredPaintersLock = new Object();

	//	private SelectAnnotationRulerAction rulerListener;

	public EditorListener() {
		super();
		System.out.println("EditorListener.EditorListener() - asdf!");
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
		if (FeaturerPlugin.DEBUG_PART_LISTENER)
			System.out.println("EditorListener.partOpened(" + partRef.getPartName() + ")");

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
		if (FeaturerPlugin.DEBUG_PART_LISTENER)
			System.out.println("EditorListener.partClosed(" + partRef.getPartName() + ")");

		IWorkbenchPart part = partRef.getPart(true);
		if (part != null && part instanceof IEditorPart) {
			unconfigureJavaEditor((IEditorPart) part);
		}
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) { if (FeaturerPlugin.DEBUG_PART_LISTENER) printPainters(); }

	@Override
	public void partVisible(IWorkbenchPartReference partRef) { if (FeaturerPlugin.DEBUG_PART_LISTENER) printPainters(); }

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) { if (FeaturerPlugin.DEBUG_PART_LISTENER) printPainters(); }

	@Override
	public void partActivated(IWorkbenchPartReference partRef) { if (FeaturerPlugin.DEBUG_PART_LISTENER) printPainters(); }

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) { if (FeaturerPlugin.DEBUG_PART_LISTENER) printPainters(); }

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) { if (FeaturerPlugin.DEBUG_PART_LISTENER) printPainters(); }

	/**
	 * Register the painter in the editor.
	 * @param editorPart the editor to be configured
	 */
	private void configureJavaEditor(IEditorPart editorPart) {
		if (editorPart instanceof JavaEditor) {
			if (FeaturerPlugin.DEBUG_PART_LISTENER) {
				System.out.println("\nConfiguring editor (" + editorPart.getTitle() + ")");
			}

			ISourceViewer viewer = ((JavaEditor) editorPart).getViewer();


			//			Object object = ((JavaEditor) editorPart).getAdapter(AnnotationPainter.class);
			//			if (object != null) {
			//				if (object instanceof AnnotationPainter)
			//					System.out.println("object IS AnnotationPainter!");
			//				else
			//					System.out.println("object is NOT AnnotationPainter...");
			//			}
			//			else {
			//				System.out.println("object IS null...");
			//			}

			//			((JavaEditor) editorPart).getSourceViewerDecorationSupport(viewer);







			if (FeaturerPlugin.DEBUG_PART_LISTENER) {
				IAnnotationModel model = viewer.getAnnotationModel();
				if (model == null)
					System.out.println("  - annotationModel == null...");
				else
					System.out.println("annotationModel OK!");
			}

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

			/* Registering listener vertical ruler */
			Object obj = ((AbstractDecoratedTextEditor) editorPart).getAdapter(IVerticalRulerInfo.class);
			if (obj != null && obj instanceof IVerticalRulerInfoExtension) {
				System.out.println("obj IS IVerticalRulerInfoExtension");
				((IVerticalRulerInfoExtension) obj).addVerticalRulerListener(new VerticalRulerListener());
				if (obj instanceof CompositeRuler)
					((CompositeRuler) obj).fireAnnotationSelected(null);
				else
					System.out.println("obj is NOT CompositeRuler");
			}
			else
				System.out.println("obj is NOT IVerticalRulerInfoExtension");


			/* Registering mouse listener. Workaround for the vertical ruler listener. */
			if (obj instanceof IVerticalRulerInfo) {
				Control widget = ((IVerticalRulerInfo) obj).getControl();
				widget.addMouseListener(new RulerMouseListener(editorPart));
			}
			//			Control widget = viewer.getTextWidget();
			IVerticalRulerInfoExtension c;



			/* Saving editor and painter for deactivation later */
			synchronized(registeredPaintersLock) {
				registeredPainters.put(viewer, painter);

				if (FeaturerPlugin.DEBUG_PART_LISTENER) {
					System.out.println("   - Painter added");
					printPainters();
				}
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

				/* Unregistering listener vertical ruler */
				Object obj = ((JavaEditor) editorPart).getAdapter(IVerticalRulerInfo.class);
				if (obj != null && obj instanceof IVerticalRulerInfoExtension) {
					//					((IVerticalRulerInfoExtension) obj).removeVerticalRulerListener(listener);
				}

				painter.deactivate(true);

				synchronized(registeredPaintersLock) {
					registeredPainters.remove(viewer);
				}
			}
			else
				if (FeaturerPlugin.DEBUG_PART_LISTENER) {
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
		if (FeaturerPlugin.DEBUG_PART_LISTENER)
			System.out.println("Updating opened editors...");


		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			IEditorReference editors[] = page.getEditorReferences();
			for (IEditorReference iEditorReference : editors) {
				IEditorPart editor = iEditorReference.getEditor(true);
				configureJavaEditor(editor);
			}
		} else {
			if (FeaturerPlugin.DEBUG_PART_LISTENER)
				System.out.println("  - active page is null...");

		}
	}

	/**
	 * prints all registered painters.
	 */
	private void printPainters() {
		System.out.println("\n-- Painters --");
		synchronized(registeredPaintersLock) {
			for (Entry<ISourceViewer, CustomAnnotationPainter> entry : registeredPainters.entrySet()) {
				ISourceViewer viewer = entry.getKey();
				if (viewer instanceof JavaSourceViewer) {
					System.out.println("  File: " + ((JavaSourceViewer) viewer).getInput());
				}
				else {
					System.out.println("  NOT Java Editor: " + viewer.getDocument());
				}
				System.out.println("\n");
			}
		}
		System.out.println("--------------\n");
	}


	/**
	 * returns the painter associated to the viewer.
	 * @param viewer the viewer to get the painter
	 * @return the painter associated with the viewer
	 */
	public static IPainter getPainter(ISourceViewer viewer) {
		synchronized(registeredPaintersLock) {
			IPainter painter = registeredPainters.get(viewer);
			return painter;
		}
	}

}
