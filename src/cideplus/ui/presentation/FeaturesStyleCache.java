package cideplus.ui.presentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import cideplus.FeaturerPlugin;
import cideplus.model.Feature;
import cideplus.ui.configuration.FeaturesConfigurationUtil;

public class FeaturesStyleCache implements IResourceChangeListener {

	private static FeaturesStyleCache instance = null;

	/*
	 * { :project => { :file => [:marker1, :marker2] } }
	 */
	private Map<IProject, Map<IFile, SortedSet<IMarker>>> markerStyleCache = null;

	private FeaturesStyleCache() {
		AnnotationPainter ap;
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
		if (FeaturerPlugin.DEBUG_CACHE)
			System.out.println("Adding marker to cache...");

		SortedSet<IMarker> markers = getStyleCache(marker);
		markers.add(marker);
	}

	public void removeMarkerFromCache(IMarker marker) {
		if (FeaturerPlugin.DEBUG_CACHE)
			System.out.println("Removing marker from cache...");

		SortedSet<IMarker> markers = getStyleCache(marker);
		markers.remove(marker);
	}

	/*
	 * Este método é chamado pelo método resourceChanged(), quando um marker
	 * é modificado. Nesse caso, o marker passado é o marker antes da modificação.
	 * Para que o cache não fique com um marker com dados errados, o marker
	 * é retirado, e depois o cache é atualizado.
	 */
	public void updateMarkerInCache(IMarker marker) {
		removeMarkerFromCache(marker);
		addMarkerToCache(marker);
		//		removeMarkerFromCache(marker);
		//		updateStyleCache(false);
	}


	public Collection<StyleRange> getStyles(IFile file) {
		SortedSet<IMarker> markers = getStyleCache(file);
		Collection<StyleRange> styles = new ArrayList<StyleRange>();
		for (IMarker marker : markers) {
			styles.add(getMarkerStyle(marker));
		}
		return styles;
	}

	private SortedSet<IMarker> getStyleCache(IMarker marker) {
		IFile file = (IFile) marker.getResource().getAdapter(IFile.class);
		return getStyleCache(file);
	}

	private SortedSet<IMarker> getStyleCache(IFile file) {
		IProject project = file.getProject();
		Map<IFile, SortedSet<IMarker>> fileToMarkerMap;
		if (markerStyleCache.containsKey(project)) {
			fileToMarkerMap = markerStyleCache.get(project);
		}
		else {
			fileToMarkerMap = new HashMap<IFile, SortedSet<IMarker>>();
			markerStyleCache.put(project, fileToMarkerMap);
		}

		SortedSet<IMarker> markerSet;
		if (fileToMarkerMap.containsKey(file)) {
			markerSet = fileToMarkerMap.get(file);
		}
		else {
			//			markerSet = new HashMap<IMarker, StyleRange>();
			markerSet = new ConcurrentSkipListSet<IMarker>(new Comparator<IMarker>() {
				public int compare(IMarker marker1, IMarker marker2) {
					if (marker1.equals(marker2)){
						return 0;
					}
					else {
						int offset1 = marker1.getAttribute("charStart", -1);
						int offset2 = marker2.getAttribute("charStart", -1);
						if (offset1 > offset2)
							return 1;
						else
							return -1;
					}
				}
			});
			fileToMarkerMap.put(file, markerSet);
		}
		return markerSet;
	}


	private void updateStyleCache(boolean resetCache) {
		if (FeaturerPlugin.DEBUG_STYLE_CACHE)
			System.out.println("Updating Style Cache...");

		if (markerStyleCache == null) {
			if (FeaturerPlugin.DEBUG_STYLE_CACHE)
				System.out.println("  -> Initializing style cache...");
			markerStyleCache = new HashMap<IProject, Map<IFile, SortedSet<IMarker>>>();
		}

		if (resetCache) {
			if (FeaturerPlugin.DEBUG_STYLE_CACHE)
				System.out.println("  -> Reseting style cache...");
			markerStyleCache.clear();
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
			int featureId = marker.getAttribute("featureId", -2);
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
				StyleRange range = new StyleRange(rangeStart, rangeLength, null, rangeColor);
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
		for (Map.Entry<IProject, Map<IFile, SortedSet<IMarker>>> project : markerStyleCache.entrySet()) {
			System.out.println(project.getKey().getName());
			for (Map.Entry<IFile, SortedSet<IMarker>> file : project.getValue().entrySet()) {
				System.out.println("  " + file.getKey().getName());
				for (IMarker marker : file.getValue()) {
					int charStart = marker.getAttribute("charStart", -1);
					int length = marker.getAttribute("charEnd", -1) - charStart;
					System.out.print("   - start: " + charStart);
					System.out.print(" / length: " + length);
					System.out.println(" / markerId: " + marker.getId());
				}
			}
		}
		System.out.println("========================================\n\n");
	}
}
