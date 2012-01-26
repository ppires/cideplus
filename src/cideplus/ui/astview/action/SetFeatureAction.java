package cideplus.ui.astview.action;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import cideplus.model.Feature;
import cideplus.ui.astview.ASTView;
import cideplus.ui.configuration.FeaturesConfigurationUtil;

public class SetFeatureAction extends Action {
	/**
	 * 
	 */
	private final ASTView astView;
	private final Feature feature;
	private final ASTNode node;

	public SetFeatureAction(ASTView astView, Feature feature, ASTNode node) {
		this.astView = astView;
		this.feature = feature;
		this.node = node;
	}

	@Override
	public int getStyle() {
		return AS_CHECK_BOX;
	}

	@Override
	public void setChecked(boolean checked) {
		if(checked){
			this.astView.getCompilationUnitFeaturesManager().setFeature(node, feature);
		} else {
			this.astView.getCompilationUnitFeaturesManager().removeFeature(node, feature);
		}
		try {
			//refreshAST();
			this.astView.getViewer().refresh(node);
			this.astView.getCompilationUnitFeaturesManager().commitChanges();
			ASTNode compilationUnit = node;
			while(!(compilationUnit instanceof CompilationUnit)){
				compilationUnit = compilationUnit.getParent();
			}
			
			FeaturesConfigurationUtil.updateEditors(astView.getSite().getShell().getDisplay(), compilationUnit);
		} catch (CoreException e) {
			MessageDialog.openError(
					this.astView.getSite().getShell(),
					"Featurer",
					"Could not save features for compilation unit. "+e.getMessage()+".");
			e.printStackTrace();
		}
	}

	@Override
	public boolean isEnabled() {
		return node != null;
	}

	@Override
	public boolean isChecked() {
		return this.astView.getCompilationUnitFeaturesManager().hasFeature(node, feature);
	}

	@Override
	public String getText() {
		return feature.getName();
	}

}