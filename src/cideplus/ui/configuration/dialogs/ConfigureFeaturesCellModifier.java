package cideplus.ui.configuration.dialogs;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.TableItem;

import cideplus.model.Feature;

public class ConfigureFeaturesCellModifier implements ICellModifier {

	private TableViewer tableViewer;

	public ConfigureFeaturesCellModifier(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}

	public boolean canModify(Object element, String property) {
		Feature feature = (Feature) element;
		return !property.equals("id");//a propriedade ID não pode ser editada
	}

	public Object getValue(Object element, String property) {
		Feature feature = (Feature) element;
		if(property.equals("rgb")){
			return new RGB(
					feature.getRgb().getRed(),
					feature.getRgb().getGreen(), 
					feature.getRgb().getBlue());
		} else if(property.equals("name")){
			return feature.getName();
		}
		return "Unknown property "+property;
	}

	public void modify(Object element, String property, Object value) {
		Feature feature = (Feature) ((TableItem)element).getData();
		if(property.equals("name")){
			feature.setName((String) value);
			tableViewer.refresh(feature);
		} else if (property.equals("rgb")) {
			RGB rgb = (RGB) value;
			feature.setRgb(new cideplus.model.RGB(rgb.red, rgb.green, rgb.blue));
			tableViewer.refresh(feature);
		}
	}

}
