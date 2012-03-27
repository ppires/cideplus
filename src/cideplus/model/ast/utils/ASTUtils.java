package cideplus.model.ast.utils;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.ITextSelection;

import cideplus.automation.Util;
import cideplus.utils.PluginUtils;

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

	public static ASTNode getNodeFromSelection(ICompilationUnit compilationUnit, ITextSelection selection) {
		ASTNode node = NodeFinder.perform(Util.getAst(compilationUnit), selection.getOffset(), selection.getLength());
		return node;
	}

	public static ASTNode getNodeFromSelection(ITextSelection selection) {
		ICompilationUnit compUnit = PluginUtils.getCurrentCompilationUnit();
		return getNodeFromSelection(compUnit, selection);
	}
}
