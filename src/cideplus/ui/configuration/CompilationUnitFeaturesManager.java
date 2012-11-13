package cideplus.ui.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.text.edits.RangeMarker;

import cideplus.model.ASTNodeReference;
import cideplus.model.CompilationUnitFeaturesModel;
import cideplus.model.Feature;
import cideplus.model.FeaturesUtil;
import cideplus.ui.presentation.FeaturesMarker;

public class CompilationUnitFeaturesManager implements ICompilationUnitFeaturesManager {

	private ICompilationUnit compilationUnit;
	private CompilationUnitFeaturesModel model;
	private IFile file;

	private List<RangeMarker> rangeMarkers;

	public CompilationUnitFeaturesManager(CompilationUnitFeaturesModel model, ICompilationUnit compilationUnit) {
		rangeMarkers = new ArrayList<RangeMarker>();
		this.model = model;
		this.compilationUnit = compilationUnit;
		IPath path = compilationUnit.getPath().removeFileExtension().addFileExtension("feat");
		file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	}

	public boolean hasFeature(ASTNode astNode, Feature feature) {
		return getASTFeatures(astNode).contains(feature);
	}

	public void setFeature(ASTNode astNode, Feature feature) {
		if(astNode == null){
			throw new IllegalArgumentException("astNode cannot be null to set feature");
		}
		getASTFeatures(astNode).add(feature);

		/* Um marker associado com cada feature. */
		try {
			FeaturesMarker.createMarker(astNode, feature.getId());
		} catch (CoreException e) {
			System.out.println("Could not create marker for feature " + feature.getName());
			e.printStackTrace();
		}

		/* Cria um RangeMarker para trackear as modificaÃ§Ãµes no texto */
		//								if (rangeMarkers == null)
		//									rangeMarkers = new ArrayList<RangeMarker>();
		RangeMarker rangeMarker = new RangeMarker(astNode.getStartPosition(), astNode.getLength());
		rangeMarkers.add(rangeMarker);
		System.out.println("rangeMarkers.size(): " + rangeMarkers.size());
		System.out.println("getRangeMarkers().size(): " + getRangeMarkers().size());
	}

	public Set<Feature> getFeatures(ASTNode astNode) {
		return getASTFeatures(astNode);
	}

	public Set<Feature> getFeatures(ASTNodeReference reference){
		return model.getFeatures(reference);
	}

	public void removeFeature(ASTNode node, Feature feature) {
		getASTFeatures(node).remove(feature);
	}

	public synchronized void commitChanges() throws CoreException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FeaturesUtil.saveFeaturesForCompilationUnit(out, model);
		ByteArrayInputStream source = new ByteArrayInputStream(out.toByteArray());
		if(file.exists()){
			file.setContents(source, true, false, null);
		} else {
			file.create(source, true, null);
		}
	}

	public Set<ASTNodeReference> getNodeReferences() {
		return model.getNodeReferences();
	}

	public ICompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	private Set<Feature> getASTFeatures(ASTNode astNode) {
		if(astNode == null){
			return new HashSet<Feature>();
		}
		return model.getFeatures(new ASTNodeReference(astNode), true);
		//return model.getFeatures(getNodeReferenceFromAST(astNode), true);
	}

	public List<RangeMarker> getRangeMarkers() {
		System.out.println("getting rangeMarkers! size(): " + rangeMarkers.size());
		return rangeMarkers;
	}

}
