package cideplus.ui.configuration;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;

import cideplus.model.ASTNodeReference;
import cideplus.model.Feature;

public interface ICompilationUnitFeaturesManager {

	boolean hasFeature(ASTNode astNode, Feature feature);

	void setFeature(ASTNode astNode, Feature feature);

	Set<Feature> getFeatures(ASTNode astNode);

	Set<Feature> getFeatures(ASTNodeReference reference);

	void removeFeature(ASTNode node, Feature feature);

	void commitChanges() throws CoreException;

	ICompilationUnit getCompilationUnit();

	Set<ASTNodeReference> getNodeReferences();

}
