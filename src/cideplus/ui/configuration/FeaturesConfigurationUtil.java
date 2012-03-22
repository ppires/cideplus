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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.source.AnnotationBarHoverManager;
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
 * Class utilit�ria para trabalhar com a configura��o das features junto a Interface Gr�fica
 * @author rogel
 *
 */
public class FeaturesConfigurationUtil {

	public static final String FEATURES_FILE = "features.feat";

	private static Map<IProject, IFeaturesManager> projectCache = new HashMap<IProject, IFeaturesManager>();

	public static IFeaturesManager getFeaturesManager(final IProject project){
		IFeaturesManager featuresManager;
		if((featuresManager = projectCache.get(project)) == null){
			if (FeaturerPlugin.DEBUG_MANAGER_CACHE)
				System.out.println("projectCache MISS");
			featuresManager = new IFeaturesManager() {

				Map<ICompilationUnit, ICompilationUnitFeaturesManager> compUnitCache = new HashMap<ICompilationUnit, ICompilationUnitFeaturesManager>();

				public void saveFeatures(Set<Feature> features) throws CoreException {
					FeaturesConfigurationUtil.saveFeatures(project, features);
				}

				public Set<Feature> getFeatures() throws CoreException, IOException {
					return FeaturesConfigurationUtil.getFeatures(project);
				}

				public IProject getProject() {
					return project;
				}

				public ICompilationUnitFeaturesManager getManagerForFile(final IFile file) throws IOException, FeatureNotFoundException, CoreException {
					ICompilationUnit compilationUnit = (ICompilationUnit) JavaCore.create(file);
					return getManagerForFile(compilationUnit);
				}

				public ICompilationUnitFeaturesManager getManagerForFile(final ICompilationUnit compilationUnit) throws IOException, FeatureNotFoundException, CoreException {
					ICompilationUnitFeaturesManager compilationUnitFeaturesManager;
					if((compilationUnitFeaturesManager = compUnitCache.get(compilationUnit)) == null){
						if (FeaturerPlugin.DEBUG_MANAGER_CACHE)
							System.out.println("compUnitCache MISS\n");
						IPath path = compilationUnit.getPath().removeFileExtension().addFileExtension("feat");
						final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
						final CompilationUnitFeaturesModel model;
						if(file.exists()){
							model = FeaturesUtil.loadFeaturesForCompilationUnit(getFeatures(), file.getContents(true));
						} else {
							model = new CompilationUnitFeaturesModel();
						}

						//						compilationUnitFeaturesManager = new CompilationUnitFeaturesManager(model, compilationUnit);
						compilationUnitFeaturesManager = new ICompilationUnitFeaturesManager() {

							public Set<ASTNodeReference> getNodeReferences() {
								return model.getNodeReferences();
							}

							private Set<Feature> getASTFeatures(ASTNode astNode) {
								if(astNode == null){
									return new HashSet<Feature>();
								}
								return model.getFeatures(getNodeReferenceFromAST(astNode), true);
							}

							public void setFeature(ASTNode astNode, Feature feature) {
								if(astNode == null)
									throw new IllegalArgumentException("astNode cannot be null to set feature");

								getASTFeatures(astNode).add(feature);

								/* Um marker associado com cada feature. */
								try {
									FeaturesMarker.createMarker(astNode, feature.getId());
									AnnotationBarHoverManager m;
								} catch (CoreException e) {
									System.out.println("Could not create marker for feature " + feature.getName());
									e.printStackTrace();
								}
							}

							public boolean hasFeature(ASTNode astNode, Feature feature) {
								return getASTFeatures(astNode).contains(feature);
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
			if (FeaturerPlugin.DEBUG_MANAGER_CACHE) System.out.println("projectCache HIT");
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
		Set<Feature> features = getFeatures(project);
		for (Feature feature : features) {
			if (feature.getId() == featureId) {
				return feature;
			}
		}
		// TODO: Verificar o impacto de lançar uma exception aqui
		//       ao invés de retornar null
		//		throw new FeatureNotFoundException(featureId);
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
