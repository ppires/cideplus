package cideplus.model;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;

import cideplus.automation.Util;
import cideplus.model.ast.utils.NodeFinder;
import cideplus.model.exceptions.FeatureNotFoundException;
import cideplus.ui.configuration.FeaturesConfigurationUtil;
import cideplus.ui.configuration.ICompilationUnitFeaturesManager;
import cideplus.ui.configuration.IFeaturesManager;
import cideplus.utils.PluginUtils;

/**
 * Classe utilit�ria para trabalhar com as features.
 * As funcionalidades dessa classe s�o independentes da interface gr�fica
 * @author rogel
 *
 */
public class FeaturesUtil {

	private static final String BYTES_OFFSET_SEPARATOR = "@";
	private static final String NUMBER_INFORMATION_SEPARATOR = "@@";
	private static final String FEATURES_END = "\06<<<";
	private static final String FEATURES_SEPARATOR = "\06>>>FEATURES:";

	/**
	 * L� as features de determinado arquivo
	 * @param contents
	 * @return
	 * @throws IOException
	 */
	public static Set<Feature> readFeatures(InputStream contents) throws IOException {
		Set<Feature> features = new TreeSet<Feature>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(contents));
		try {
			String featureLine;
			while ((featureLine = reader.readLine()) != null) {
				try {
					String[] properties = featureLine.split(Feature.FEATURE_PROPERTY_DELIMITER);
					Integer id = new Integer(properties[0]);
					String name = properties[1];
					RGB rgb = RGB.fromString(properties[2]);
					features.add(new Feature(id, name, rgb));
				} catch (RuntimeException e) {
					throw new RuntimeException("Invalid feature: '"+featureLine+"'. \nFeature should have the folowing pattern: [id]::[name]::([red],[green],[blue])");
				}
			}
		} finally {
			reader.close();
		}
		return features;
	}

	/**
	 * Salva as features em determinado arquivo
	 * @param out
	 * @param features
	 */
	public static void saveFeatures(OutputStream out, Set<Feature> features) {
		PrintWriter writer = new PrintWriter(out);
		try {
			for (Feature feature : features) {
				writer.println(feature.toString());
			}
			writer.flush();
		} finally {
			writer.close();
		}
	}

	public static CompilationUnitFeaturesModel loadFeaturesForCompilationUnit(Set<Feature> projectFeatures, InputStream in) throws IOException, FeatureNotFoundException {
		CompilationUnitFeaturesModel model = new CompilationUnitFeaturesModel();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String linha = null;
		ByteArrayOutputStream astOut = new ByteArrayOutputStream();
		PrintWriter astWriter = new PrintWriter(astOut);
		int[] features = null;
		while((linha = reader.readLine()) != null){
			String numberInformation = linha.substring(0, linha.indexOf(NUMBER_INFORMATION_SEPARATOR));
			String[] bytesOffset = numberInformation.split(BYTES_OFFSET_SEPARATOR);
			int bytes = Integer.parseInt(bytesOffset[0]);
			int offset = Integer.parseInt(bytesOffset[1]);
			linha = linha.substring(linha.indexOf(NUMBER_INFORMATION_SEPARATOR) + NUMBER_INFORMATION_SEPARATOR.length());
			if(linha.contains(FEATURES_SEPARATOR) && linha.contains(FEATURES_END)){
				String featuresString = linha.substring(linha.indexOf(FEATURES_SEPARATOR) + FEATURES_SEPARATOR.length(), linha.length() - FEATURES_END.length() - 1);
				String[] featuresSplit = featuresString.split(",");
				features = new int[featuresSplit.length];
				for (int i = 0; i < featuresSplit.length; i++) {
					features[i] = Integer.parseInt(featuresSplit[i]);
				}
				linha = linha.substring(0, linha.indexOf(FEATURES_SEPARATOR));
			}
			astWriter.print(linha);
			if(features != null){
				astWriter.flush();
				String astString = new String(astOut.toByteArray());
				for (int feature_id : features) {
					Feature featureObj = new Feature(feature_id);
					if(!projectFeatures.contains(featureObj)){
						throw new FeatureNotFoundException(feature_id);
					}
					//pegar o feature completo do projeto, não apenas com o ID
					for (Feature feature : projectFeatures) {
						if(featureObj.equals(feature)){
							featureObj = feature;
							break;
						}
					}
					model.getFeatures(new ASTNodeReference(astString, bytes, offset)).add(featureObj);
				}
				astOut.reset();
			}
		}
		return model;
	}

	public static void saveFeaturesForCompilationUnit(OutputStream out, CompilationUnitFeaturesModel model){
		PrintWriter writer = new PrintWriter(out);
		try {
			for (ASTNodeReference nodeReference : model.getNodeReferences()) {
				Set<Feature> features = model.getFeatures(nodeReference);
				if (features.size() > 0) {
					writer.print(nodeReference.bytes);
					writer.print(BYTES_OFFSET_SEPARATOR);
					writer.print(nodeReference.offset);
					writer.print(NUMBER_INFORMATION_SEPARATOR);
					writer.print(nodeReference);
					writer.print(FEATURES_SEPARATOR);
					for (Feature feature : features) {
						writer.print(feature.getId());
						writer.print(",");
					}
					writer.println(FEATURES_END);
				}
			}
		} finally {
			writer.close();
		}
	}

	/* Marca uma feature a partir de um offset e um length */
	public static void markFeature(int feature_id, int offset, int length) throws CoreException, IOException, FeatureNotFoundException {
		IProject project = PluginUtils.getCurrentProject();
		ICompilationUnit compUnit = PluginUtils.getCurrentCompilationUnit();
		IFeaturesManager manager = FeaturesConfigurationUtil.getFeaturesManager(project);
		ICompilationUnitFeaturesManager managerForFile = manager.getManagerForFile(compUnit);
		ASTNode node = NodeFinder.perform(Util.getAst(compUnit), offset, length);
		if (node == null) {
			System.out.println("No node found...");
		}
		else {
			Feature feature = FeaturesConfigurationUtil.getFeature(feature_id, project);
			managerForFile.setFeature(node, feature);
			managerForFile.commitChanges();
		}
	}
}
