package cideplus;

import java.io.InputStream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import cideplus.ui.astview.ASTViewPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class FeaturerPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "cideplus"; //$NON-NLS-1$

	// The shared instance
	private static FeaturerPlugin plugin;
	private final ASTViewPlugin astViewPlugin;
	//	private static EditorListener editorListener;


	public static final boolean DEBUG_AST_MARKER = false;
	public static final boolean DEBUG_HOVER = false;
	public static final boolean DEBUG_MANAGER_CACHE = false;
	public static final boolean DEBUG_MARKERS = false;
	public static final boolean DEBUG_PRESENTATION = false;
	public static final boolean DEBUG_REFRESH = false;
	public static final boolean DEBUG_RESOURCE_LISTENER = false;
	public static final boolean DEBUG_RULER_LISTENER = false;
	public static final boolean DEBUG_SELECTION = false;
	public static final boolean DEBUG_STYLE_CACHE = false;
	public static final boolean DEBUG_PART_LISTENER = true;

	public ASTViewPlugin getAstViewPlugin() {
		return astViewPlugin;
	}

	/**
	 * The constructor
	 */
	public FeaturerPlugin() {
		astViewPlugin = new ASTViewPlugin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		astViewPlugin.start(context);
		//		installEditorListener();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		//		uninstallEditorListener();
		astViewPlugin.stop(context);
		super.stop(context);
	}

	public static InputStream getFile(String path) {
		try {
			return FileLocator.find(getDefault().getBundle(),
					new Path("/icons" + path), null).openStream();
		} catch (Exception e3) {
			throw new RuntimeException("Não foi possivel encontrar o arquivo "
					+ path);
		}
	}

	public static Image getImage(Device device, String path) {
		return new Image(device, FeaturerPlugin.getFile(path));
	}

	/* M�TODOS UTILITARIOS */

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static FeaturerPlugin getDefault() {
		return plugin;
	}

	//	private void installEditorListener() {
	//		IWorkbench workbench = PlatformUI.getWorkbench();
	//		if (workbench != null) {
	//			editorListener = new EditorListener();
	//			IPartService service = (IPartService) workbench.getService(IPartService.class);
	//			service.addPartListener(editorListener);
	//		}
	//		else {
	//			if (FeaturerPlugin.DEBUG_PART_LISTENER)
	//				System.out.println("workbench == null (install)");
	//		}
	//	}
	//
	//	private void uninstallEditorListener() {
	//		IWorkbench workbench = PlatformUI.getWorkbench();
	//		if (workbench != null) {
	//			IPartService service = (IPartService) workbench.getService(IPartService.class);
	//			service.removePartListener(editorListener);
	//			editorListener = null;
	//		}
	//		else {
	//			if (FeaturerPlugin.DEBUG_PART_LISTENER)
	//				System.out.println("workbench == null (uninstall)");
	//		}
	//	}
}
