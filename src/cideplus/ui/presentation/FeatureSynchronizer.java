package cideplus.ui.presentation;

import java.io.IOException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IStartup;

import cideplus.FeaturerPlugin;
import cideplus.model.FeaturesUtil;
import cideplus.model.exceptions.FeatureNotFoundException;

public class FeatureSynchronizer implements IStartup, IResourceChangeListener {

	/**
	 * Register for marker changes notification
	 */
	@Override
	public void earlyStartup() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.PRE_BUILD);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER) {
			System.out.print("\n\nBuild kind: ");
			switch (event.getBuildKind()) {
				case IncrementalProjectBuilder.AUTO_BUILD:
					System.out.println("AUTO_BUILD");
					break;
				case IncrementalProjectBuilder.CLEAN_BUILD:
					System.out.println("CLEAN_BUILD");
					break;
				case IncrementalProjectBuilder.FULL_BUILD:
					System.out.println("FULL_BUILD");
					break;
				case IncrementalProjectBuilder.INCREMENTAL_BUILD:
					System.out.println("INCREMENTAL_BUILD");
					break;
				default:
					System.out.println("None of them...");
					break;
			}
		}

		IMarkerDelta[] markerDeltas = event.findMarkerDeltas(FeaturesMarker.TYPE, false);
		for (IMarkerDelta delta : markerDeltas) {
			int kind = delta.getKind();

			switch (kind) {
				case IResourceDelta.ADDED:
					if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER)
						System.out.println("Marker ADDED!");
					//					getInstance().addMarkerToCache(delta.getMarker());
					break;

				case IResourceDelta.REMOVED:
					if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER)
						System.out.println("Marker REMOVED!");
					//					getInstance().removeMarkerFromCache(delta.getMarker());
					break;

				case IResourceDelta.CHANGED:
					if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER)
						System.out.println("\nMarker CHANGED! (PRE)");

					int oldOffset = delta.getAttribute("charStart", -1);
					int oldLength = delta.getAttribute("charEnd", -1) - oldOffset;
					int oldFeatureId = delta.getAttribute("featureId", -1);
					IMarker marker = delta.getMarker();
					int offset = marker.getAttribute("charStart", -1);
					int length = marker.getAttribute("charEnd", -1) - offset;
					int featureId = marker.getAttribute("featureId", -1);
					if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER) {
						System.out.println("offset: " + oldOffset + "/" + offset);
						System.out.println("length: " + oldLength + "/" + length);
						System.out.println("feature id: " + oldFeatureId + "/" + featureId);
					}
					try {
						FeaturesUtil.unmarkFeature(oldFeatureId, oldOffset, oldLength);
					} catch (FeatureNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
			}
		}
	}
}

