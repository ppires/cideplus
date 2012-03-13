package cideplus;

import java.io.InputStream;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import cideplus.ui.astview.ASTViewPlugin;
import cideplus.ui.presentation.FeaturesStyleCache;

/**
 * The activator class controls the plug-in life cycle
 */
public class FeaturerPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "cideplus"; //$NON-NLS-1$

	// The shared instance
	private static FeaturerPlugin plugin;
	private final ASTViewPlugin astViewPlugin;

	public static final boolean DEBUG_RESOURCE_LISTENER = true;
	public static final boolean DEBUG_PRESENTATION = false;
	public static final boolean DEBUG_STYLE_CACHE = true;
	public static final boolean DEBUG_MARKERS = false;
	public static final boolean DEBUG_REFRESH = false;
	public static final boolean DEBUG_CACHE = false;

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
		ResourcesPlugin.getWorkspace().addResourceChangeListener(FeaturesStyleCache.getInstance(), IResourceChangeEvent.POST_CHANGE);
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

}
