package cideplus.ui.export.dialog;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.TableItem;

import cideplus.model.Feature;

public class SelectFeaturesCellModifier implements ICellModifier {

	private TableViewer tableViewer;
	private Map<Feature, Boolean> values = new HashMap<Feature, Boolean>();

	public SelectFeaturesCellModifier(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}

	public boolean canModify(Object element, String property) {
		Feature feature = (Feature) element;
		return property.equals("id");//a propriedade ID apenas será editada
	}

	public Object getValue(Object element, String property) {
		Feature feature = (Feature) element;
		if(property.equals("id")){
			return isChecked(feature);
		}
		return "Unknown property "+property;
	}

	public boolean isChecked(Feature feature) {
		Boolean value;
		if((value = values.get(feature)) != null){
			return value;
		}
		return false;
	}

	public void modify(Object element, String property, Object v) {
		Feature feature = (Feature) ((TableItem)element).getData();
		if(property.equals("id")){
			values.put(feature, (Boolean) v);
			tableViewer.refresh(feature);
		}
	}

}