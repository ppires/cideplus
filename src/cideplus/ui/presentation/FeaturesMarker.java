package cideplus.ui.presentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;

import cideplus.FeaturerPlugin;
import cideplus.model.ast.utils.ASTUtils;
import cideplus.utils.PluginUtils;

public class FeaturesMarker {

	public static final String TYPE = "cideplus.markers.featuresMarker";


	public static IMarker createMarker(IResource resource, final int offset, final int length, int featureId) throws CoreException {
		Map<String, Object> attributes = createMarkerAttributes(offset, length, featureId);
		IMarker marker = resource.createMarker(TYPE);
		marker.setAttributes(attributes);

		if (FeaturerPlugin.DEBUG_MARKERS)
			printAllRelatedMarkers(resource, false);

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

	public static void addAnnotation(IMarker marker, int offset, int length) {
		ITextEditor editor = PluginUtils.getCurrentTextEditor();

		//The DocumentProvider enables to get the document currently loaded in the editor
		IDocumentProvider idp = editor.getDocumentProvider();

		//This is the document we want to connect to. This is taken from the current editor input.
		IDocument document = idp.getDocument(editor.getEditorInput());

		//The IannotationModel enables to add/remove/change annoatation to a Document loaded in an Editor
		IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());

		//Note: The annotation type id specify that you want to create one of your annotations
		FeatureAnnotation annotation = new FeatureAnnotation(marker);

		//Finally add the new annotation to the model
		iamf.connect(document);
		iamf.addAnnotation(annotation, new Position(offset, length));
		iamf.disconnect(document);
	}

	public static void addAnnotation(IMarker marker, ITextSelection selection) {
		addAnnotation(marker, selection.getOffset(), selection.getLength());
	}




	/* returns a list of a resource's markers */
	public static List<IMarker> findMarkers(IResource resource) {
		try {
			return Arrays.asList(resource.findMarkers(TYPE, true, IResource.DEPTH_ZERO));
		} catch (CoreException e) {
			return new ArrayList<IMarker>();
		}
	}

	/* Returns the marker associated with the given feature in the AST node */
	public static IMarker getCorrespondingMarker(ASTNode node, int featureId) {
		int nodeStart = node.getStartPosition();
		int nodeLength = node.getLength();
		IResource resource = ASTUtils.getCorrespondingResource(node);
		List<IMarker> markers = findAllRelatedMarkers(resource);

		if (FeaturerPlugin.DEBUG_AST_MARKER) {
			System.out.println("Getting corresponding marker...");
			for (IMarker marker : markers)
				printMarkerInline(marker);
			System.out.println("---");
			System.out.println("nodeStart: " + nodeStart);
			System.out.println("nodeLength: " + nodeLength);
		}

		Iterator<IMarker> it = markers.iterator();
		while (it.hasNext()) {
			IMarker marker = it.next();
			//		for (IMarker marker : markers) {
			int markerStart = marker.getAttribute("charStart", -1);
			int markerLength = marker.getAttribute("charEnd", -1) - markerStart;
			if (marker.getAttribute("featureId", -1) == featureId) {
				if (FeaturerPlugin.DEBUG_AST_MARKER) {
					System.out.println("  markerStart: " + markerStart);
					System.out.println("  markerLength: " + markerLength);
				}
				if (markerStart == nodeStart && markerLength == nodeLength)
					return marker;
			}
		}
		return null;
	}


	/* Returns a list of markers that are linked to the resource or any sub resource of the resource */
	public static List<IMarker> findAllRelatedMarkers(IResource  resource) {
		try {
			return Arrays.asList(resource.findMarkers(TYPE, true, IResource.DEPTH_INFINITE));
		} catch (CoreException e) {
			return new ArrayList<IMarker>();
		}
	}

	/* Returns a list of markers that are linked to the resource or any sub resource of the resource */
	public static List<IMarker> findAllMarkers() {
		IWorkspaceRoot root = PluginUtils.getWorkspaceRoot();
		try {
			return Arrays.asList(root.findMarkers(TYPE, true, IResource.DEPTH_INFINITE));
		} catch (CoreException e) {
			return new ArrayList<IMarker>();
		}
	}

	/**
	 * Creates an attributes map for a text marker that references a range in text. In order to the marker
	 * appear automatically in the vertical ruler, the <code>lineNumber</code> or the <code>charStart</code>/<code>charEnd</code>
	 * attributes must be set.
	 * 
	 * @param offset		The offset of the range being referenced
	 * @param length		The length of the range being referenced
	 * @param featureId		The ID of the feature this marker is tracking
	 * @return The attribute map.
	 * @author ppires
	 */
	private static Map<String, Object> createMarkerAttributes(int offset, int length, int featureId) {
		Map<String, Object> attributes = new HashMap<String, Object>();

		// Setting the line number attribute
		IDocument document = PluginUtils.getCurrentDocument();
		if (document != null) {
			try {
				int lineNumber = document.getLineOfOffset(offset) + 1;
				MarkerUtilities.setLineNumber(attributes, lineNumber);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		// Setting the charStart and charEnd attributes.
		MarkerUtilities.setCharStart(attributes, offset);
		MarkerUtilities.setCharEnd(attributes, offset + length);
		attributes.put("featureId", new Integer(featureId));
		attributes.put("length", new Integer(length));
		return attributes;
	}


	public static void printAllRelatedMarkers(IResource resource, boolean inline) {
		System.out.println("========================================");
		System.out.println("Printing all markers related to " + resource.getName());
		System.out.println("========================================");
		for (IMarker marker : findAllRelatedMarkers(resource)) {
			if (inline)
				printMarkerInline(marker);
			else {
				printMarker(marker);
				System.out.println(System.getProperty("line.separator"));
			}
		}
		System.out.println("========================================");
	}

	public static void printAllMarkers(boolean inline) {
		for (IMarker marker : findAllMarkers()) {
			if (inline)
				printMarkerInline(marker);
			else {
				printMarker(marker);
				System.out.println(System.getProperty("line.separator"));
			}
		}
	}

	public static void printMarkerInline(IMarker marker) {
		int charStart = marker.getAttribute("charStart", -1);
		int charEnd = marker.getAttribute("charEnd", -1);
		int lineNumber = marker.getAttribute("lineNumber", -1);
		int featureId = marker.getAttribute("featureId", -1);
		System.out.print("   - start: " + charStart);
		System.out.print(" / length: " + (charEnd - charStart));
		System.out.print(" / line: " + lineNumber);
		System.out.print(" / featureId: " + featureId);
		System.out.println(" / markerId: " + marker.getId());
	}

	public static void printMarker(IMarker marker) {
		try {
			int start = (Integer) marker.getAttribute("charStart");
			int end = (Integer) marker.getAttribute("charEnd");
			System.out.println("type: " + marker.getType());
			System.out.println("resource: " + marker.getResource().getName());
			System.out.println("char start: " + start);
			System.out.println("length: " + (end - start));
			System.out.println("feature id: " + marker.getAttribute("featureId"));
			System.out.println("message: " + marker.getAttribute("message"));
		} catch (CoreException e) {
			System.out.println("Caught Exception getting marker att...");
			e.printStackTrace();
		}
	}
}


