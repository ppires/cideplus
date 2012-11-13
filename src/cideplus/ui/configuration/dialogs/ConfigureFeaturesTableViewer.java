package cideplus.ui.configuration.dialogs;

import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import cideplus.model.Feature;

public final class ConfigureFeaturesTableViewer implements	IStructuredContentProvider {
	
	public ConfigureFeaturesTableViewer() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}

	public void dispose() {
		
	}

	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		return ((Set<Feature>)inputElement).toArray();
	}
}