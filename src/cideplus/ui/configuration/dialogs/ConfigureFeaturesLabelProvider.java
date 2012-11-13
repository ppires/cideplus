package cideplus.ui.configuration.dialogs;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import cideplus.model.Feature;
import cideplus.ui.configuration.FeaturesConfigurationUtil;

public class ConfigureFeaturesLabelProvider implements ITableLabelProvider {

	protected Device device;

	public ConfigureFeaturesLabelProvider(Device device) {
		super();
		this.device = device;
	}

	public void addListener(ILabelProviderListener listener) {

	}

	public void removeListener(ILabelProviderListener listener) {

	}
	
	public void dispose() {

	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if(columnIndex == 2){//montar o RGB
			RGB color = FeaturesConfigurationUtil.getRGB((Feature)element);
			RGB black = new RGB(0, 0, 0);
			PaletteData dataPalette = new PaletteData(new RGB[] { black, black, color });
			ImageData imageData = new ImageData(16, 16, 4, dataPalette);
			imageData.transparentPixel = 0;
			int size = 16;
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					if(i == 0 || j == 0 || i == size -1 || j == size -1 ||
							i == 1 || j == 1 || i == size -2 || j == size -2){
						imageData.setPixel(i, j, 0);
					} else if (i == 2 || j == 2 || i == size -3 || j == size -3) {
						imageData.setPixel(i, j, 1);
					} else {
						imageData.setPixel(i, j, 2);
					}
				}
			}
	        ImageData mask = imageData.getTransparencyMask();
	        Image image = new Image(device, imageData, mask);
			return image;
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		switch(columnIndex)
		{
			case 0:
				return ((Feature)element).getId().toString();
			case 1:
				return ((Feature)element).getName();
			case 2:
				return ((Feature)element).getRgb().toString();
			default:
				return "Invalid column: " + columnIndex;
		}
	}

}
