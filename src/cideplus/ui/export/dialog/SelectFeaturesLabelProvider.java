package cideplus.ui.export.dialog;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

import cideplus.FeaturerPlugin;
import cideplus.model.Feature;
import cideplus.ui.configuration.dialogs.ConfigureFeaturesLabelProvider;

public class SelectFeaturesLabelProvider extends ConfigureFeaturesLabelProvider {

	private SelectFeaturesCellModifier cellModifier;

	public SelectFeaturesLabelProvider(Device device, SelectFeaturesCellModifier cellModifier) {
		super(device);
		this.cellModifier = cellModifier;
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if(columnIndex == 0){
			return "";
		}
		return super.getColumnText(element, columnIndex);
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if(columnIndex == 0){
			Feature feature = (Feature)element;
			boolean checked = cellModifier.isChecked(feature);
			if(checked){
				return FeaturerPlugin.getImage(device, "/checked.gif");
			} else {
				return FeaturerPlugin.getImage(device, "/unchecked.gif");
			}
		}
		return super.getColumnImage(element, columnIndex);
	}
}
