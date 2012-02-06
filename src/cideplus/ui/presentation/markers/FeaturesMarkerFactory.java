package cideplus.ui.presentation.markers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.MarkerUtilities;

import cideplus.automation.Util;
import cideplus.model.Feature;
import cideplus.model.FeaturerException;
import cideplus.model.ASTUtils.NodeFinder;
import cideplus.ui.configuration.CompilationUnitFeaturesManager;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.FeaturesManager;
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

		ICompilationUnit compUnit = PluginUtils.getCurrentCompilationUnit();
		FeaturesManager manager = FeaturesConfigurationUtil.getFeaturesManager(resource.getProject());
		CompilationUnitFeaturesManager managerForFile;

		// Getting manager
		try {
			managerForFile = manager.getManagerForFile(compUnit);
		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (FeaturerException e) {
			System.out.println("FeaturerException");
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		FeaturesManager featuresManager = FeaturesConfigurationUtil.getFeaturesManager(resource.getProject());

		// Finding feature
		ASTNode node = NodeFinder.perform(Util.getAst(compUnit), selection.getOffset(), selection.getLength());
		if (node == null) {
			System.out.println("No node found...");
		}
		else {
			Set<Feature> features;

			try {
				features = featuresManager.getFeatures();
			} catch (IOException e) {
				System.out.println("IOException");
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			System.out.println("total features: " + features.size());
			Iterator<Feature> it = features.iterator();
			if (it.hasNext()) {
				Feature feature = it.next();
				System.out.println(feature);
				managerForFile.setFeature(node, feature);
				managerForFile.commitChanges();
			}
			else {
				System.out.println("no features...");
			}
		}


		return marker;
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

	private static HashMap<String, Object> createMarkerAttributes(ITextSelection selection, int feature_id) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		//MarkerUtilities.setLineNumber(attributes, selection.getStartLine());
		MarkerUtilities.setCharStart(attributes, selection.getOffset());
		MarkerUtilities.setCharEnd(attributes, selection.getOffset() + selection.getLength());
		attributes.put("feature_id", new Integer(feature_id));
		attributes.put("length", new Integer(selection.getLength()));
		return attributes;
	}

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

	public static void printAllMarkers() {
		for (IMarker marker : findAllMarkers()) {
			printMarker(marker);
			System.out.println(System.getProperty("line.separator"));
		}
	}
}


