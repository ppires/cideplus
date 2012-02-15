package cideplus.model.ast.utils;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTUtils {

	public static IResource getCorrespondingResource(ASTNode node) {
		ASTNode root = node.getRoot();
		if (root.getNodeType() == ASTNode.COMPILATION_UNIT) {
			Object resource = ((CompilationUnit) root).getJavaElement().getAdapter(IResource.class);
			if (resource != null) {
				return (IResource) resource;
			}
		}
		return null;
	}
}
