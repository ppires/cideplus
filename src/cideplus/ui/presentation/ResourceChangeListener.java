package cideplus.ui.presentation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import cideplus.FeaturerPlugin;


public class ResourceChangeListener implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		IResource resource = event.getResource();
		switch (event.getType()) {
		case IResourceChangeEvent.PRE_CLOSE:
			if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER) System.out.println("Project " + resource.getFullPath() + " is about to close.");
			break;
		case IResourceChangeEvent.PRE_DELETE:
			if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER) System.out.println("Project " + resource.getFullPath() + " is about to be deleted.");
			break;
		case IResourceChangeEvent.POST_CHANGE:
			if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER) System.out.println("Resources have changed.");
			try {
				event.getDelta().accept(new DeltaPrinter());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case IResourceChangeEvent.PRE_BUILD:
			if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER) System.out.println("Build about to run.");
			try {
				event.getDelta().accept(new DeltaPrinter());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case IResourceChangeEvent.POST_BUILD:
			if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER) System.out.println("Build complete.");
			try {
				event.getDelta().accept(new DeltaPrinter());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
	}
}

class DeltaPrinter implements IResourceDeltaVisitor {
	public boolean visit(IResourceDelta delta) {
		IResource res = delta.getResource();
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			//			System.out.print("Resource ");
			//			System.out.print(res.getFullPath());
			//			System.out.println(" was added.");
			break;
		case IResourceDelta.REMOVED:
			//			System.out.print("Resource ");
			//			System.out.print(res.getFullPath());
			//			System.out.println(" was removed.");
			break;
		case IResourceDelta.CHANGED:
			//			System.out.print("Resource ");
			//			System.out.print(delta.getFullPath());
			//			System.out.println(" has changed.");
			int flags = delta.getFlags();
			if ((flags & IResourceDelta.CONTENT) != 0) {
				if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER)
					System.out.println("--> Content Changed");

				Object adaptedResource = delta.getAdapter(IResource.class);
				if (adaptedResource != null) {
					IResource resource = (IResource) adaptedResource;
					if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER)
						System.out.println("delta adapted resource = " + resource.getName());
				}
				else {
					if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER)
						System.out.println("Could not adapt delta to resource...");
				}


			}
			if ((flags & IResourceDelta.REPLACED) != 0) {
				if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER) System.out.println("--> Content Replaced");
			}
			if ((flags & IResourceDelta.MARKERS) != 0) {
				if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER) System.out.println("--> Marker Changed");
				//				IMarkerDelta[] markers = delta.getMarkerDeltas();
				//				for (IMarkerDelta markerDelta : markers) {
				//					IMarker marker = markerDelta.getMarker();
				//					try {
				//						if (marker.exists() && marker.getType() == FeaturesMarkerFactory.FEATURES_MARKER_ID) {
				//							//FeaturesMarkerFactory.printMarker(marker);
				//						}
				//					} catch (CoreException e) {
				//						// TODO Auto-generated catch block
				//						e.printStackTrace();
				//					}
				//				}
			}
			break;
		}
		return true; // visit the children
	}
}
