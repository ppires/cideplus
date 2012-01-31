package cideplus.ui.presentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

import cideplus.utils.PluginUtils;

public class FeaturesMarkerFactory {

	public static final String FEATURES_MARKER_ID = "cideplus.markers.featuresMarker";

	/*
	 * Creates a Marker
	 */
	public static IMarker createMarker(IResource resource, ITextSelection selection, int feature_id) throws CoreException {
		HashMap<String, Object> attributes = createMarkerAttributes(selection, feature_id);
		IMarker marker = resource.createMarker(FEATURES_MARKER_ID);
		marker.setAttributes(attributes);
		return marker;
	}

	//	public static void createMarker(IResource resource, ITextSelection selection)
	//	throws CoreException {
	//		int char_end = selection.getOffset() + selection.getLength();
	//		HashMap<String, Object>map = new HashMap<String, Object>();
	//		MarkerUtilities.setLineNumber(map, selection.getStartLine());
	//		MarkerUtilities.setCharStart(map, selection.getOffset());
	//		MarkerUtilities.setCharEnd(map, char_end);
	//		map.put("feature_id", new Integer(666));
	//		MarkerUtilities.createMarker(resource, map, MARKER_ID);
	//		System.out.println("marker created!");
	//	}

	//	public static void addAnnotation(IMarker marker, ITextSelection selection) {
	//		ITextEditor editor = PluginUtils.getCurrentTextEditor();
	//
	//		//The DocumentProvider enables to get the document currently loaded in the editor
	//		IDocumentProvider idp = editor.getDocumentProvider();
	//
	//		//This is the document we want to connect to. This is taken from the current editor input.
	//		IDocument document = idp.getDocument(editor.getEditorInput());
	//
	//		//The IannotationModel enables to add/remove/change annoatation to a Document loaded in an Editor
	//		IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());
	//
	//		//Note: The annotation type id specify that you want to create one of your annotations
	//		SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(ANNOTATION_ID, marker);
	//
	//		//Finally add the new annotation to the model
	//		iamf.connect(document);
	//		iamf.addAnnotation(ma,new Position(selection.getOffset(),selection.getLength()));
	//		iamf.disconnect(document);
	//	}




	/* returns a list of a resource's markers */
	public static List<IMarker> findMarkers(IResource resource) {
		try {
			return Arrays.asList(resource.findMarkers(FEATURES_MARKER_ID, true, IResource.DEPTH_ZERO));
		} catch (CoreException e) {
			return new ArrayList<IMarker>();
		}
	}


	/* Returns a list of markers that are linked to the resource or any sub resource of the resource */
	public static List<IMarker> findAllMarkers(IResource  resource) {
		try {
			return Arrays.asList(resource.findMarkers(FEATURES_MARKER_ID, true, IResource.DEPTH_INFINITE));
		} catch (CoreException e) {
			return new ArrayList<IMarker>();
		}
	}

	/* Returns a list of markers that are linked to the resource or any sub resource of the resource */
	public static List<IMarker> findAllMarkers() {
		IWorkspaceRoot root = PluginUtils.getWorkspaceRoot();
		try {
			return Arrays.asList(root.findMarkers(FEATURES_MARKER_ID, true, IResource.DEPTH_INFINITE));
		} catch (CoreException e) {
			return new ArrayList<IMarker>();
		}
	}


	/*
	 * Returns the selection of the package explorer
	 */
	public static TreeSelection getTreeSelection() {
		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if(selection instanceof TreeSelection){
			return (TreeSelection)selection;
		}
		return null;
	}

	public static void printMarker(IMarker marker) {
		try {
			int start = (Integer) marker.getAttribute("charStart");
			int end = (Integer) marker.getAttribute("charEnd");
			System.out.println("type: " + marker.getType());
			System.out.println("resource: " + marker.getResource().getName());
			System.out.println("char start: " + start);
			System.out.println("length: " + (end - start));
			System.out.println("feature id: " + marker.getAttribute("feature_id"));
			System.out.println("message: " + marker.getAttribute("message"));
		} catch (CoreException e) {
			System.out.println("Caught Exception getting marker att...");
			e.printStackTrace();
		}
	}

	private static HashMap<String, Object> createMarkerAttributes(ITextSelection selection, int feature_id) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		//MarkerUtilities.setLineNumber(attributes, selection.getStartLine());
		MarkerUtilities.setCharStart(attributes, selection.getOffset());
		MarkerUtilities.setCharEnd(attributes, selection.getOffset() + selection.getLength());
		attributes.put("feature_id", new Integer(feature_id));
		return attributes;
	}
}


