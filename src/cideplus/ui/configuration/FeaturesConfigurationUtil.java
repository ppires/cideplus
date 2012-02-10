package cideplus.ui.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import cideplus.model.ASTNodeReference;
import cideplus.model.CompilationUnitFeaturesModel;
import cideplus.model.Feature;
import cideplus.model.FeaturesUtil;
import cideplus.model.exceptions.FeatureNotFoundException;
import cideplus.ui.editor.FeaturerCompilationUnitEditor;
import cideplus.utils.PluginUtils;

/**
 * Class utilit�ria para trabalhar com a configura��o das features junto a Interface Gr�fica
 * @author rogel
 *
 */
public class FeaturesConfigurationUtil {

	public static final String FEATURES_FILE = "features.feat";

	private static Map<IProject, FeaturesManager> cache = new HashMap<IProject, FeaturesManager>();

	public static FeaturesManager getFeaturesManager(final IProject project){
		FeaturesManager featuresManager;
		if((featuresManager = cache.get(project)) == null){
			featuresManager = new FeaturesManager() {

				Map<ICompilationUnit, CompilationUnitFeaturesManager> cache = new HashMap<ICompilationUnit, CompilationUnitFeaturesManager>();

				public void saveFeatures(Set<Feature> features) throws CoreException {
					FeaturesConfigurationUtil.saveFeatures(project, features);
				}

				public Set<Feature> getFeatures() throws CoreException, IOException {
					return FeaturesConfigurationUtil.getFeatures(project);
				}

				public CompilationUnitFeaturesManager getManagerForFile(final ICompilationUnit compilationUnit) throws IOException, FeatureNotFoundException, CoreException {
					CompilationUnitFeaturesManager compilationUnitFeaturesManager;
					if((compilationUnitFeaturesManager = cache.get(compilationUnit)) == null){
						PluginUtils.showPopup("(compilationUnitFeaturesManager = cache.get(compilationUnit)) == null");
						IPath path = compilationUnit.getPath().removeFileExtension().addFileExtension("feat");
						final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
						final CompilationUnitFeaturesModel model;
						if(file.exists()){
							model = FeaturesUtil.loadFeaturesForCompilationUnit(getFeatures(), file.getContents(true));
						} else {
							model = new CompilationUnitFeaturesModel();
						}

						compilationUnitFeaturesManager = new CompilationUnitFeaturesManager() {

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
								if(astNode == null){
									throw new IllegalArgumentException("astNode cannot be null to set feature");
								}
								getASTFeatures(astNode).add(feature);

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
						cache.put(compilationUnit, compilationUnitFeaturesManager);
					}
					return compilationUnitFeaturesManager;
				}
			};
			//o project feature manager nao possuir� cache... apenas o compilation unit
			//cache.put(project, featuresManager);
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
	public static Feature getFeature(int feature_id, IProject project) throws CoreException, IOException {
		Set<Feature> features = getFeatures(project);
		for (Feature feature : features) {
			if (feature.getId() == feature_id) {
				return feature;
			}
		}
		//		throw new FeatureNotFoundException(feature_id);
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
		if(astNode == null){
			throw new IllegalArgumentException("Parameter astNode cannot be null");
		}
		int bytes = astNode.getLength();
		int offset = astNode.getStartPosition();
		String node = "";
		do {
			node += "$$";
			if(astNode instanceof CompilationUnit){
				node += "COMPILATION UNIT;;";
			} else if (astNode instanceof TypeDeclaration) {
				node += astNode.getClass().getSimpleName()+": "+((TypeDeclaration)astNode).getName()+" <<==\n";
			} else if (astNode instanceof MethodDeclaration) {
				Type returnType = ((MethodDeclaration)astNode).getReturnType2();
				SimpleName methodName = ((MethodDeclaration)astNode).getName();
				@SuppressWarnings("rawtypes")
				List parameters = ((MethodDeclaration)astNode).parameters();
				node += astNode.getClass().getSimpleName()+": "+returnType+" "+methodName+parameters+" <<==\n";
			} else if (astNode instanceof Block) {
				Block block = (Block) astNode;
				node += astNode.getClass().getSimpleName()+": "+block.properties()+"  <<==\n";
			} else if (astNode instanceof IfStatement) {
				IfStatement statement = (IfStatement) astNode;
				node += astNode.getClass().getSimpleName()+": "+statement.getExpression()+"  <<==\n";
			} else if (astNode instanceof DoStatement) {
				DoStatement statement = (DoStatement) astNode;
				node += astNode.getClass().getSimpleName()+": "+statement.getExpression()+"  <<==\n";
			} else if(astNode instanceof ForStatement) {
				ForStatement statement = (ForStatement) astNode;
				node += astNode.getClass().getSimpleName()+": "+statement.initializers()+" "+statement.getExpression()+" "+statement.updaters()+"  <<==\n";
			} else {
				node += astNode.getClass().getSimpleName()+": "+astNode+" <<==\n";
			}
			astNode = astNode.getParent();
		} while(astNode != null);
		node = node.replace('\n', ' ').replace('\r', ' ');
		return new ASTNodeReference(node, bytes, offset);
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
		cache = new HashMap<IProject, FeaturesManager>();
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
					//se o compilation unit � null, atualizar todos os editores
					refresh(iEditorReference);
				}
			}
		}
	}

	private static void refresh(IEditorReference iEditorReference) {
		IEditorPart editor = iEditorReference.getEditor(false);
		if(editor instanceof FeaturerCompilationUnitEditor){
			FeaturerCompilationUnitEditor fcue = (FeaturerCompilationUnitEditor) editor;
			fcue.getColorPresentation().refreshFeatures();
			ISourceViewer viewer = fcue.getViewer();
			viewer.invalidateTextPresentation();
		}
	}
}
