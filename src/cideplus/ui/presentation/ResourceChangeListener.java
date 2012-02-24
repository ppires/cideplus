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
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			try {
				event.getDelta().accept(new DeltaPrinter());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

class DeltaPrinter implements IResourceDeltaVisitor {
	public boolean visit(IResourceDelta delta) {
		IResource resource = delta.getResource();

		switch (delta.getKind()) {
		case IResourceDelta.CHANGED:
			int flags = delta.getFlags();

			if ((flags & IResourceDelta.CONTENT) != 0) {
				if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER) {
					System.out.println("--> Content Changed");

				}


			}
			if ((flags & IResourceDelta.REPLACED) != 0) {
				if (FeaturerPlugin.DEBUG_RESOURCE_LISTENER) System.out.println("--> Content Replaced");
			}
			break;
		}
		return true; // visit the children
	}
}