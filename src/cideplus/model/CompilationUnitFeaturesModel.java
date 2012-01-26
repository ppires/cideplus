package cideplus.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CompilationUnitFeaturesModel {

	Map<ASTNodeReference, Set<Feature>> configurationMap = new HashMap<ASTNodeReference, Set<Feature>>();
	
	public Set<Feature> getFeatures(ASTNodeReference reference){
		return getFeatures(reference, false);
	}
	
	public Set<Feature> getFeatures(ASTNodeReference reference, boolean updateAstReference){
		Set<Feature> list = configurationMap.get(reference);
		if(list == null) {
			list = new TreeSet<Feature>();
		}
		if(updateAstReference){
			configurationMap.remove(reference);
		}
		configurationMap.put(reference, list);
		return list;
	}

	public Set<ASTNodeReference> getNodeReferences() {
		HashSet<ASTNodeReference> set = new HashSet<ASTNodeReference>();
		set.addAll(configurationMap.keySet());
		return set;
	}
	
	
}
