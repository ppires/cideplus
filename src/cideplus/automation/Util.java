package cideplus.automation;

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;

import cideplus.model.Feature;
import cideplus.ui.configuration.IFeaturesManager;

public class Util {

	public static Set<Feature> getSafeFeatures(final IFeaturesManager featuresManager){
		try {
			return featuresManager.getFeatures();
		} catch (Exception e) {
			MessageDialog.openError(null, "Erro", "Erro ao ler as features do projeto. "+e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public static CompilationUnit getAst(ICompilationUnit compUnit) {
		return getAst(compUnit, true);
		//return SharedASTProvider.getAST(compUnit, SharedASTProvider.WAIT_YES, new NullProgressMonitor());
	}

	public static CompilationUnit getAst(ICompilationUnit compUnit, boolean resolveBindings) {
		ASTParser astParser = ASTParser.newParser(AST.JLS3);
		astParser.setResolveBindings(true);
		astParser.setBindingsRecovery(true);
		astParser.setStatementsRecovery(true);
		astParser.setSource(compUnit);
		return (CompilationUnit) astParser.createAST(null);
		//return SharedASTProvider.getAST(compUnit, SharedASTProvider.WAIT_YES, new NullProgressMonitor());
	}
}
