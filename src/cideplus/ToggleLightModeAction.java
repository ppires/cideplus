package cideplus;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ToggleLightModeAction implements IObjectActionDelegate {

	public ToggleLightModeAction() {
		// TODO Auto-generated constructor stub
		if (FeaturerPlugin.DEBUG_LIGHT_MODE)
			System.out.println("ToggleLightModeAction.ToggleLightModeAction()");
	}

	@Override
	public void run(IAction action) {
		if (FeaturerPlugin.DEBUG_LIGHT_MODE)
			System.out.println("ToggleLightModeAction.run()");

		FeaturerPlugin.toggleLightMode();

		//		ISourceViewer viewer = PluginUtils.getCurrentSourceViewer();
		//		IPainter painter = EditorListener.getPainter(viewer);
		//		if (painter instanceof CustomAnnotationPainter) {
		//			CustomAnnotationPainter customPainter = (CustomAnnotationPainter) painter;
		//			customPainter.toggleLightMode();
		//
		//			// test code
		//			IAnnotationModel model = viewer.getAnnotationModel();
		//			Iterator<Annotation> it = model.getAnnotationIterator();
		//			while (it.hasNext()) {
		//				Annotation a = it.next();
		//				if (!(a instanceof SimpleMarkerAnnotation)) {
		//					System.out.println("a NOT instanceof SimpleMarkerAnnotation");
		//					continue;
		//				}
		//				else {
		//					IMarker marker = ((SimpleMarkerAnnotation) a).getMarker();
		//					String type = null;
		//					try {
		//						type = marker.getType();
		//					} catch (CoreException e) {
		//						// TODO Auto-generated catch block
		//						e.printStackTrace();
		//					}
		//					System.out.println("marker   type: " + type);
		//					System.out.println("features type: " + FeaturesMarker.TYPE);
		//					if (type.equals(FeaturesMarker.TYPE)) {
		//						customPainter.setAnnotationToPaint(a);
		//					}
		//					else {
		//						System.out.println("marker is NOT FeaturesMarker");
		//					}
		//				}
		//			}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		if (FeaturerPlugin.DEBUG_LIGHT_MODE)
			System.out.println("ToggleLightModeAction.selectionChanged()");
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		if (FeaturerPlugin.DEBUG_LIGHT_MODE)
			System.out.println("ToggleLightModeAction.setActivePart()");
	}

}
