package cideplus.ui.presentation;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import cideplus.FeaturerPlugin;
import cideplus.model.Feature;
import cideplus.ui.configuration.FeaturesConfigurationUtil;

public class FeaturesStyleCache implements IResourceChangeListener {

	private static FeaturesStyleCache instance = null;

	/*
	 * { :project => { :file => { :marker => :style } } }
	 */
	private Map<IProject, Map<IFile, Map<IMarker, StyleRange>>> markerStyleCache = null;

	private FeaturesStyleCache() {
		updateStyleCache(false);
	}

	public static synchronized FeaturesStyleCache getInstance() {
		if (instance == null)
			instance = new FeaturesStyleCache();

		return instance;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (false)
			updateStyleCache(true);
		else {
			IMarkerDelta[] markerDeltas = event.findMarkerDeltas(FeaturesMarker.TYPE, false);
			for (IMarkerDelta delta : markerDeltas) {
				int kind = delta.getKind();

				switch (kind) {
				case IResourceDelta.ADDED:
					if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER)
						System.out.println("Marker ADDED!");
					getInstance().addMarkerToCache(delta.getMarker());
					break;

				case IResourceDelta.REMOVED:
					if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER)
						System.out.println("Marker REMOVED!");
					getInstance().removeMarkerFromCache(delta.getMarker());
					break;

				case IResourceDelta.CHANGED:
					if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER)
						System.out.println("Marker CHANGED!");
					getInstance().updateMarkerInCache(delta.getMarker());
					break;
				}
			}
			if (FeaturerPlugin.DEBUG_STYLE_CACHE)
				getInstance().printStyleCache();
		}
	}

	public void addMarkerToCache(IMarker marker) {
		Map<IMarker, StyleRange> markerToStyleCache = getStyleCache(marker);
		StyleRange style = getMarkerStyle(marker);
		markerToStyleCache.put(marker, style);
	}

	public void removeMarkerFromCache(IMarker marker) {
		if (FeaturerPlugin.DEBUG_CACHE)
			System.out.println("Removing marker form cache...");
		Map<IMarker, StyleRange> markerToStyleCache = getStyleCache(marker);
		markerToStyleCache.remove(marker);
	}

	/*
	 * Este método é chamado pelo método resourceChanged(), quando um marker
	 * é modificado. Nesse caso, o marker passado é o marker antes da modificação.
	 * Para que o cache não fique com um marker com dados errados, o marker
	 * é retirado, e depois o cache é atualizado.
	 */
	public void updateMarkerInCache(IMarker marker) {
		addMarkerToCache(marker);
		//		removeMarkerFromCache(marker);
		//		updateStyleCache(false);
	}


	public Collection<StyleRange> getStyles(IFile file) {
		return getStyleCache(file).values();
	}

	private Map<IMarker, StyleRange> getStyleCache(IMarker marker) {
		IFile file = (IFile) marker.getResource().getAdapter(IFile.class);
		return getStyleCache(file);
	}

	private Map<IMarker, StyleRange> getStyleCache(IFile file) {
		IProject project = file.getProject();
		Map<IFile, Map<IMarker, StyleRange>> fileToMarkerMap;
		if (markerStyleCache.containsKey(project)) {
			fileToMarkerMap = markerStyleCache.get(project);
		}
		else {
			fileToMarkerMap = new HashMap<IFile, Map<IMarker, StyleRange>>();
			markerStyleCache.put(project, fileToMarkerMap);
		}

		Map<IMarker, StyleRange> markerToStyleMap;
		if (fileToMarkerMap.containsKey(file)) {
			markerToStyleMap = fileToMarkerMap.get(file);
		}
		else {
			markerToStyleMap = new HashMap<IMarker, StyleRange>();
			fileToMarkerMap.put(file, markerToStyleMap);
		}
		return markerToStyleMap;
	}


	private void updateStyleCache(boolean resetCache) {
		if (FeaturerPlugin.DEBUG_STYLE_CACHE)
			System.out.println("Updating Style Cache...");

		if (resetCache) {
			if (FeaturerPlugin.DEBUG_STYLE_CACHE)
				System.out.println("  -> Reseting style cache...");
			markerStyleCache.clear();
		}

		if (markerStyleCache == null) {
			if (FeaturerPlugin.DEBUG_STYLE_CACHE)
				System.out.println("  -> Initializing style cache...");
			markerStyleCache = new HashMap<IProject, Map<IFile, Map<IMarker, StyleRange>>>();
		}

		List<IMarker> markers = FeaturesMarker.findAllMarkers();
		for (IMarker marker : markers) {
			addMarkerToCache(marker);
			if (FeaturerPlugin.DEBUG_STYLE_CACHE)
				System.out.println("  -> Added marker");
		}

		if (FeaturerPlugin.DEBUG_STYLE_CACHE)
			System.out.println("  -> Done!\n");
	}


	private StyleRange getMarkerStyle(IMarker marker) {
		try {
			int featureId = marker.getAttribute("featureId", 0);
			IProject project = ((IFile) marker.getResource().getAdapter(IFile.class)).getProject();
			Feature feature = FeaturesConfigurationUtil.getFeature(project, featureId);
			if (feature == null) {
				System.out.println("\n\n\nNULL FEATURE!!!\n\n\n");
			}
			else {
				RGB rgb = new RGB(feature.getRgb().getRed(), feature.getRgb().getGreen(), feature.getRgb().getBlue());
				Color rangeColor = new Color(null, rgb);
				int rangeStart = marker.getAttribute("charStart", 0);
				int rangeLength = marker.getAttribute("charEnd", 0) - rangeStart;
				StyleRange range = new StyleRange();
				range.background = rangeColor;
				range.start = rangeStart;
				range.length = rangeLength;
				return range;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	public void printStyleCache() {
		System.out.println("========================================");
		System.out.println("Style Cache");
		System.out.println("========================================");
		for (Map.Entry<IProject, Map<IFile, Map<IMarker, StyleRange>>> project : markerStyleCache.entrySet()) {
			System.out.println(project.getKey().getName());
			for (Map.Entry<IFile, Map<IMarker, StyleRange>> file : project.getValue().entrySet()) {
				System.out.println("  " + file.getKey().getName());
				for (Map.Entry<IMarker, StyleRange> marker : file.getValue().entrySet()) {
					System.out.print("   - start: " + marker.getValue().start);
					System.out.print(" / length: " + marker.getValue().length);
					System.out.print(" / " + marker.getValue().background);
					System.out.println(" / markerId: " + marker.getKey().getId());
				}
			}
		}
		System.out.println("========================================\n\n");
	}
}
