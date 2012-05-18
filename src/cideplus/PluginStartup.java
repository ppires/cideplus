package cideplus;

import org.eclipse.ui.IPartService;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import cideplus.ui.editor.EditorListener;

public class PluginStartup implements IStartup {

	private static EditorListener editorListener;

	@Override
	public void earlyStartup() {
		System.out.println("PluginStartup.earlyStartup()");

		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				installEditorListener();
			}

		});
	}


	private void installEditorListener() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		editorListener = new EditorListener();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window != null) {
			IPartService service = window.getPartService();
			if (service != null) {
				service.addPartListener(editorListener);

				if (FeaturerPlugin.DEBUG_PART_LISTENER)
					System.out.println("registered part listener!\n - service class: " + service.getClass());
			}
			else {
				if (FeaturerPlugin.DEBUG_PART_LISTENER)
					System.out.println("part service is null");
			}
		}
		else {
			if (FeaturerPlugin.DEBUG_PART_LISTENER)
				System.out.println("workbench window is null (no active workbench window or called from outside UI thread...)");
		}
	}

	private void uninstallEditorListener() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			IPartService service = (IPartService) workbench.getService(IPartService.class);
			if (service != null) {
				service.removePartListener(editorListener);
			}
			else {
				if (FeaturerPlugin.DEBUG_PART_LISTENER)
					System.out.println("part service is null");
			}
			editorListener = null;
		}
		else {
			if (FeaturerPlugin.DEBUG_PART_LISTENER)
				System.out.println("workbench == null (uninstall)");
		}
	}

}
