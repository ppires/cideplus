package cideplus.ui.configuration.dialogs;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Table;

public class FeaturesDialogCellEditorProvider {

	public static CellEditor[] getConfigurationCellEditors(Table table) {
		CellEditor idEditor = new TextCellEditor(table);
		CellEditor nameEditor = new TextCellEditor(table);
		CellEditor colorEditor = new FeatureColorCellEditor(table);
		return new CellEditor[]{
				idEditor,
				nameEditor,
				colorEditor
		};
	}

	public static CellEditor[] getSelectionCellEditors(Table table) {
		CellEditor idEditor = new CheckboxCellEditor(table);
		CellEditor nameEditor = new TextCellEditor(table);
		CellEditor colorEditor = new FeatureColorCellEditor(table);
		return new CellEditor[]{
				idEditor,
				nameEditor,
				colorEditor
		};
	}
 
}
