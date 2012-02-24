package cideplus.ui.presentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;

import cideplus.model.ast.utils.ASTUtils;
import cideplus.utils.PluginUtils;

public class FeaturesMarkerFactory {

	private static final boolean DEBUG_MARKERS = false;

	public static final String FEATURES_MARKER_ID = "cideplus.markers.featuresMarker";


	public static IMarker createMarker(IResource resource, int offset, int length, int featureId) throws CoreException {
		HashMap<String, Object> attributes = createMarkerAttributes(offset, length, featureId);
		IMarker marker = resource.createMarker(FEATURES_MARKER_ID);
		marker.setAttributes(attributes);
		if (DEBUG_MARKERS) printAllRelatedMarkers(resource);

		ITextEditor editor = PluginUtils.getCurrentTextEditor();
		IAnnotationModel model = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		System.out.println("Annotation model class: " + model.getClass());

		return marker;
	}

	public static IMarker createMarker(IResource resource, ITextSelection selection, int featureId) throws CoreException {
		IMarker marker = createMarker(resource, selection.getOffset(), selection.getLength(), featureId);
		return marker;
	}

	public static IMarker createMarker(ASTNode node, int featureId) throws CoreException {
		IResource resource = ASTUtils.getCorrespondingResource(node);
		if (resource != null) {
			IMarker marker = createMarker(resource, node.getStartPosition(), node.getLength(), featureId);
			return marker;
		}
		return null;
	}

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
	public static List<IMarker> findAllRelatedMarkers(IResource  resource) {
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

	private static HashMap<String, Object> createMarkerAttributes(int offset, int length, int feature_id) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		//MarkerUtilities.setLineNumber(attributes, selection.getStartLine());
		MarkerUtilities.setCharStart(attributes, offset);
		MarkerUtilities.setCharEnd(attributes, offset + length);
		attributes.put("feature_id", new Integer(feature_id));
		attributes.put("length", new Integer(length));
		return attributes;
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

	public static void printAllMarkers() {
		for (IMarker marker : findAllMarkers()) {
			printMarker(marker);
			System.out.println(System.getProperty("line.separator"));
		}
	}

	public static void printAllRelatedMarkers(IResource resource) {
		System.out.println("========================================");
		System.out.println("Printing all markers related to " + resource.getName());
		System.out.println("========================================");
		for (IMarker marker : findAllRelatedMarkers(resource)) {
			printMarker(marker);
			System.out.println(System.getProperty("line.separator"));
		}
		System.out.println("========================================");
	}
}


