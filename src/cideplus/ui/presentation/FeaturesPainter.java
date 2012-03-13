package cideplus.ui.presentation;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;

import cideplus.FeaturerPlugin;
import cideplus.utils.PluginUtils;

public class FeaturesPainter implements ITextPresentationListener {

	private FeaturesStyleCache styleCache;

	public FeaturesPainter() {
		styleCache = FeaturesStyleCache.getInstance();
	}

	public void applyTextPresentation(TextPresentation textPresentation) {
		if (FeaturerPlugin.DEBUG_PRESENTATION)
			System.out.println("Applying text presentation from resource tracker!");

		IFile file = PluginUtils.getCurrentFile();

		if (FeaturerPlugin.DEBUG_PRESENTATION)
			System.out.println("  -> Current file: " + file);

		if (file != null) {
			for (StyleRange style : styleCache.getStyles(file))
				textPresentation.mergeStyleRange(style);
		}
	}
}

