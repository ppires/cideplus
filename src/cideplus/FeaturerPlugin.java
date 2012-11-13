package cideplus;

import java.io.InputStream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import cideplus.ui.astview.ASTViewPlugin;
import cideplus.ui.editor.EditorListener;
import cideplus.ui.presentation.CustomAnnotationPainter;
import cideplus.utils.PluginUtils;

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
	public static final boolean DEBUG_PAINTER = false;
	public static final boolean DEBUG_REFRESH = false;
	public static final boolean DEBUG_RESOURCE_LISTENER = false;
	public static final boolean DEBUG_RULER_LISTENER = false;
	public static final boolean DEBUG_SELECTION = false;
	public static final boolean DEBUG_STYLE_CACHE = false;
	public static final boolean DEBUG_PART_LISTENER = false;
	public static final boolean DEBUG_LIGHT_MODE = false;
	public static final boolean DEBUG_MOUSE_LISTENER = false;
	public static final boolean DEBUG_AST_REFERENCE = true;

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
			throw new RuntimeException("NÃ£o foi possivel encontrar o arquivo "
					+ path);
		}
	}

	public static Image getImage(Device device, String path) {
		return new Image(device, FeaturerPlugin.getFile(path));
	}

	/* Mï¿½TODOS UTILITARIOS */

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static FeaturerPlugin getDefault() {
		return plugin;
	}

	/**
	 * Toggles the plugin "light mode" (hidden colors).
	 */
	public static void toggleLightMode() {
		ISourceViewer viewer = PluginUtils.getCurrentSourceViewer();
		IPainter painter = EditorListener.getPainter(viewer);
		if (painter instanceof CustomAnnotationPainter)
			((CustomAnnotationPainter) painter).toggleLightMode();
		else
			if (DEBUG_LIGHT_MODE)
				System.out.println("toggling light mode: painter is NOT CustomAnnotationPainter");
	}

}
