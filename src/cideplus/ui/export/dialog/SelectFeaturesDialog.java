package cideplus.ui.export.dialog;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import cideplus.model.Feature;
import cideplus.ui.configuration.dialogs.ConfigureFeaturesDialog;
import cideplus.ui.configuration.dialogs.FeaturesDialogCellEditorProvider;

public class SelectFeaturesDialog extends ConfigureFeaturesDialog {

	private SelectFeaturesCellModifier cellModifier;

	public SelectFeaturesDialog(Shell parentShell, IJavaProject project) {
		super(parentShell, project);
	}

	/**
	 * Mostra a caixa para seleção das features do projeto
	 * @return
	 * @throws CoreException
	 * @throws IOException
	 */
	public Set<Feature> selectFeatures() throws CoreException, IOException {
		features = getFeaturesManager().getFeatures();
		setBlockOnOpen(true);
		if(open() == Window.OK){
			for (Iterator<Feature> iterator = features.iterator(); iterator.hasNext();) {
				Feature feature = iterator.next();
				if(!getCellModifier().isChecked(feature)){
					iterator.remove();
				}
			}
			return features;
			//MessageDialog.openInformation(getShell(), "Featurer", "Features configuration saved successfully.");
		}
		return null;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select Features");
	}

	protected CellEditor[] getCellEditors(Table table) {
		return FeaturesDialogCellEditorProvider.getSelectionCellEditors(table);
	}
	
	@Override
	protected SelectFeaturesCellModifier getCellModifier() {
		if(cellModifier == null){
			this.cellModifier = new SelectFeaturesCellModifier(tableViewer);
		}
		return cellModifier;
	}
	
	@Override
	protected void createToolBar(Composite comp) {
		//para selecionar as features nao existe tool bar
	}
	
	@Override
	protected IBaseLabelProvider getLabelProvider() {
		return new SelectFeaturesLabelProvider(getShell().getDisplay(), getCellModifier());
	}
	
	@Override
	protected void configureColumns() {
		createColumn(table, "Select", 50);
		createColumn(table, "Feature", 200);
		createColumn(table, "Color", 120);
	}
}
