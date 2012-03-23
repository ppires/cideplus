package cideplus.ui.presentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import cideplus.FeaturerPlugin;
import cideplus.model.Feature;
import cideplus.ui.configuration.FeaturesConfigurationUtil;

public class StyleCache implements IResourceChangeListener {

	private static StyleCache instance = null;

	/*
	 * { :project => { :file => [:marker1, :marker2] } }
	 */
	private Map<IProject, Map<IFile, SortedSet<IMarker>>> markerStyleCache = null;
	//	private Object styleCacheLock = new Object();

	private StyleCache() {
		updateStyleCache(false);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	public static synchronized StyleCache getInstance() {
		if (instance == null)
			instance = new StyleCache();

		return instance;
	}

	public void resourceChanged(IResourceChangeEvent event) {
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
			getInstance().printStyleCache(false);
	}

	private void addMarkerToCache(IMarker marker) {
		if (FeaturerPlugin.DEBUG_STYLE_CACHE)
			System.out.println("Adding marker to cache...");

		SortedSet<IMarker> markers = getStyleCache(marker);
		boolean resp = markers.add(marker);
	}

	private void removeMarkerFromCache(IMarker oldMarker) {
		SortedSet<IMarker> markers = getStyleCache(oldMarker);

		Iterator<IMarker> it = markers.iterator();
		while (it.hasNext()) {
			IMarker marker = it.next();
			if (marker.equals(oldMarker)) {
				it.remove();
				break;
			}
		}
	}

	/*
	 * From Eclipse Docs:
	 * "Instances of IMarker do not hold the attributes themselves but
	 *  rather uniquely refer to the attribute container".
	 * 
	 *  Por isso não é necessário fazer nada quando um marker
	 *  é modificado.
	 */
	private void updateMarkerInCache(IMarker marker) {
		//...
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
			markerSet = new TreeSet<IMarker>(new Comparator<IMarker>() {
				public int compare(IMarker marker1, IMarker marker2) {
					if (marker1.equals(marker2)){
						return 0;
					}
					else {
						int offset1 = marker1.getAttribute("charStart", -1);
						int offset2 = marker2.getAttribute("charStart", -1);
						if (offset1 == offset2)
							return 0;
						else if (offset1 > offset2)
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
		if (marker.exists()) {
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
		}
		return null;
	}


	private void printStyleCache(boolean printRealMarkers) {
		System.out.println("========================================");
		System.out.println("Style Cache");
		System.out.println("========================================");
		for (Map.Entry<IProject, Map<IFile, SortedSet<IMarker>>> project : markerStyleCache.entrySet()) {
			System.out.println(project.getKey().getName());
			for (Map.Entry<IFile, SortedSet<IMarker>> file : project.getValue().entrySet()) {
				System.out.println("  " + file.getKey().getName());
				for (IMarker marker : file.getValue()) {
					FeaturesMarker.printMarkerInline(marker);
				}
				if (printRealMarkers) {
					System.out.println("--- Real Markers ---");
					List<IMarker> markers = FeaturesMarker.findAllRelatedMarkers(file.getKey());
					for (IMarker marker : markers) {
						FeaturesMarker.printMarkerInline(marker);
					}
				}
			}
		}
		System.out.println("========================================");
	}

}
