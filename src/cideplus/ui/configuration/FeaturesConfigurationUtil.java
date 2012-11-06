package cideplus.ui.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import cideplus.FeaturerPlugin;
import cideplus.model.ASTNodeReference;
import cideplus.model.CompilationUnitFeaturesModel;
import cideplus.model.Feature;
import cideplus.model.FeaturesUtil;
import cideplus.model.exceptions.FeatureNotFoundException;
import cideplus.ui.editor.FeaturerCompilationUnitEditor;
import cideplus.ui.presentation.FeaturesMarker;

/**
 * Class utilitária para trabalhar com a configuração das features junto a Interface Gráfica
 * @author rogel
 *
 */
public class FeaturesConfigurationUtil {


	public static final String FEATURES_FILE = ".features.feat";

	private static Map<IProject, IFeaturesManager> projectCache = new HashMap<IProject, IFeaturesManager>();

	public static IFeaturesManager getFeaturesManager(final IProject project){
		IFeaturesManager featuresManager;
		if((featuresManager = projectCache.get(project)) == null){
			if (FeaturerPlugin.DEBUG_MANAGER_CACHE)
				System.out.println("projectCache MISS");
			featuresManager = new IFeaturesManager() {

				Map<ICompilationUnit, ICompilationUnitFeaturesManager> compUnitCache = new HashMap<ICompilationUnit, ICompilationUnitFeaturesManager>();

				@Override
				public void saveFeatures(Set<Feature> features) throws CoreException {
					FeaturesConfigurationUtil.saveFeatures(project, features);
				}

				@Override
				public Set<Feature> getFeatures() throws CoreException, IOException {
					return FeaturesConfigurationUtil.getFeatures(project);
				}

				@Override
				public IProject getProject() {
					return project;
				}

				@Override
				public ICompilationUnitFeaturesManager getManagerForFile(final IFile file) throws IOException, FeatureNotFoundException, CoreException {
					ICompilationUnit compilationUnit = (ICompilationUnit) JavaCore.create(file);
					return getManagerForFile(compilationUnit);
				}

				@Override
				public ICompilationUnitFeaturesManager getManagerForFile(final ICompilationUnit compilationUnit) throws IOException, CoreException, FeatureNotFoundException {
					ICompilationUnitFeaturesManager compilationUnitFeaturesManager;
					if((compilationUnitFeaturesManager = compUnitCache.get(compilationUnit)) == null){
						if (FeaturerPlugin.DEBUG_MANAGER_CACHE)
							System.out.println("compUnitCache MISS\n");
						// um '.' (ponto) é colocado no início do nome do arquivo para ele não ficar visível pro usuário
						IPath path = compilationUnit.getPath().removeFileExtension().addFileExtension("feat");
						String filename = "." + path.lastSegment();
						path = path.removeLastSegments(1).append(filename);
						//System.out.println("features file: " + path);

						final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
						final CompilationUnitFeaturesModel model;
						if(file.exists()){
							model = FeaturesUtil.loadFeaturesForCompilationUnit(getFeatures(), file.getContents(true));
						} else {
							model = new CompilationUnitFeaturesModel();
						}

						//						compilationUnitFeaturesManager = new CompilationUnitFeaturesManager(model, compilationUnit);
						compilationUnitFeaturesManager = new ICompilationUnitFeaturesManager() {

							@Override
							public Set<ASTNodeReference> getNodeReferences() {
								return model.getNodeReferences();
							}

							private Set<Feature> getASTFeatures(ASTNode astNode) {
								if(astNode == null){
									return new HashSet<Feature>();
								}
								return model.getFeatures(getNodeReferenceFromAST(astNode), true);
							}

							@Override
							public void setFeature(ASTNode astNode, Feature feature) {
								if(astNode == null)
									throw new IllegalArgumentException("AST node cannot be null to set feature");

								getASTFeatures(astNode).add(feature);

								/* Um marker associado com cada feature. */
								try {
									FeaturesMarker.createMarker(astNode, feature.getId());
								} catch (CoreException e) {
									System.out.println("Could not create marker for feature " + feature.getName());
									e.printStackTrace();
								}
							}

							@Override
							public boolean hasFeature(ASTNode astNode, Feature feature) {
								return getASTFeatures(astNode).contains(feature);
							}

							@Override
							public Set<Feature> getFeatures(ASTNode astNode) {
								return getASTFeatures(astNode);
							}

							@Override
							public Set<Feature> getFeatures(ASTNodeReference reference){
								return model.getFeatures(reference);
							}

							@Override
							public void removeFeature(ASTNode node, Feature feature) {
								getASTFeatures(node).remove(feature);
								try {
									IMarker marker = FeaturesMarker.getCorrespondingMarker(node, feature.getId());
									if (marker != null)
										marker.delete();
								} catch (CoreException e) {
									e.printStackTrace();
								}
							}

							@Override
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

							@Override
							public ICompilationUnit getCompilationUnit() {
								return compilationUnit;
							}
						};
						compUnitCache.put(compilationUnit, compilationUnitFeaturesManager);
					}
					else {
						if (FeaturerPlugin.DEBUG_MANAGER_CACHE)
							System.out.println("compUnitCache HIT\n");
					}
					return compilationUnitFeaturesManager;
				}
			};
			//o project feature manager nao possuirá cache... apenas o compilation unit
			//projectCache.put(project, featuresManager);
		}
		else {
			if (FeaturerPlugin.DEBUG_MANAGER_CACHE)
				System.out.println("projectCache HIT");
		}
		return featuresManager;
	}

	public static Set<Feature> getFeatures(IProject project) throws CoreException, IOException {
		IFile featuresFile = project.getFile(FEATURES_FILE);
		Set<Feature> features;
		if(featuresFile.exists()){
			features = FeaturesUtil.readFeatures(featuresFile.getContents(true));
		} else {
			features = new TreeSet<Feature>();
		}
		return features;
	}

	//	TODO: Não iterar nas features para buscar o id.
	public static Feature getFeature(IProject project, int featureId) throws CoreException, IOException {
		if (project != null) {
			Set<Feature> features = getFeatures(project);
			for (Feature feature : features) {
				if (feature.getId() == featureId) {
					return feature;
				}
			}
			// TODO: Verificar o impacto de lançar uma exception aqui
			//       ao invés de retornar null
			//		throw new FeatureNotFoundException(featureId);
		}
		else {
			System.out.println("PluginUtils.getFeature(): project is NULL!");
		}
		return null;
	}

	private static void saveFeatures(IProject project, Set<Feature> features) throws CoreException {
		IFile featuresFile = project.getFile(FEATURES_FILE);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FeaturesUtil.saveFeatures(out, features);
		if(featuresFile.exists()){
			featuresFile.setContents(new ByteArrayInputStream(out.toByteArray()), true, false, null);
		} else {
			featuresFile.create(new ByteArrayInputStream(out.toByteArray()), true, null);
		}
	}

	private static ASTNodeReference getNodeReferenceFromAST(ASTNode astNode) {
		return new ASTNodeReference(astNode);
	}

	public static RGB getRGB(Feature feature){
		return new RGB(feature.getRgb().getRed(), feature.getRgb().getGreen(), feature.getRgb().getBlue());
	}

	public static RGB getCombinedRGB(Collection<Feature> featureList) {
		RGB rgb = new RGB(0,0,0);
		int amountRed = 0;
		int amountGreen = 0;
		int amountBlue = 0;
		if (featureList.size() > 0) {
			for (Feature feature : featureList) {
				if(feature.getRgb().getRed() > 0){
					amountRed+=1;
				}
				if(feature.getRgb().getGreen() > 0){
					amountGreen += 1;
				}
				if(feature.getRgb().getBlue() > 0){
					amountBlue +=1;
				}
				rgb.red += feature.getRgb().getRed();
				rgb.green += feature.getRgb().getGreen();
				rgb.blue += feature.getRgb().getBlue();
			}
			if(amountRed > 0){
				rgb.red /= amountRed;
			}
			if(amountGreen > 0){
				rgb.green /= amountGreen;
			}
			if(amountBlue > 0){
				rgb.blue /= amountBlue;
			}
		} else {
			rgb = new RGB(255,255,255);
		}
		return rgb;
	}

	public static void clean() {
		projectCache = new HashMap<IProject, IFeaturesManager>();
	}

	public static void updateEditors(Display display, final ASTNode compilationUnit) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				doUpdateEditors(compilationUnit);
			}
		});
	}

	private static void doUpdateEditors(ASTNode compilationUnit) {
		//atualizar os editores
		IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow iWorkbenchWindow : workbenchWindows) {
			IEditorReference[] editorReferences = iWorkbenchWindow.getActivePage().getEditorReferences();
			for (IEditorReference iEditorReference : editorReferences) {
				String editorName = iEditorReference.getName();
				if(compilationUnit != null){
					String compilationUnitName = ((CompilationUnit) compilationUnit).getJavaElement().getElementName();
					if(editorName.equals(compilationUnitName)){
						refresh(iEditorReference);
					}
				} else {
					//se o compilation unit é null, atualizar todos os editores
					refresh(iEditorReference);
				}
			}
		}
	}

	private static void refresh(IEditorReference iEditorReference) {
		IEditorPart editor = iEditorReference.getEditor(false);
		if(editor instanceof FeaturerCompilationUnitEditor){
			((FeaturerCompilationUnitEditor) editor).getViewer().invalidateTextPresentation();
			//			FeaturerCompilationUnitEditor fcue = (FeaturerCompilationUnitEditor) editor;
			//			fcue.getColorPresentation().refreshFeatures();
			//			ISourceViewer viewer = fcue.getViewer();
			//			viewer.invalidateTextPresentation();
		}
	}
}
