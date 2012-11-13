package cideplus.ui.configuration;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;

import cideplus.model.Feature;
import cideplus.model.exceptions.FeatureNotFoundException;

public interface IFeaturesManager {

	Set<Feature> getFeatures() throws CoreException, IOException;

	void saveFeatures(Set<Feature> features) throws CoreException;

	IProject getProject();

	ICompilationUnitFeaturesManager getManagerForFile(IFile file) throws IOException, FeatureNotFoundException, CoreException;

	ICompilationUnitFeaturesManager getManagerForFile(ICompilationUnit file) throws IOException, FeatureNotFoundException, CoreException;
}
