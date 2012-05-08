package cideplus.ui.presentation;

import org.eclipse.jdt.internal.ui.text.HTMLAnnotationHover;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.LineRange;

import cideplus.FeaturerPlugin;

public class FeaturesAnnotationHover extends HTMLAnnotationHover {// implements IAnnotationHoverExtension, IAnnotationHoverExtension2 {

	public FeaturesAnnotationHover() {
		super(false);

		if (FeaturerPlugin.DEBUG_HOVER)
			System.out.println("Instantiated new hover!");
	}

	public FeaturesAnnotationHover(boolean showLineNumber) {
		super(showLineNumber);

		if (FeaturerPlugin.DEBUG_HOVER)
			System.out.println("Instantiated new hover!");
	}

	public ILineRange getHoverLineRange(ISourceViewer viewer, int lineNumber) {
		return new LineRange(lineNumber, 1);
	}

	@Override
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		if (FeaturerPlugin.DEBUG_HOVER) {
			System.out.println("Getting hover info for line #" + lineNumber);
			System.out.println("Info returned by super(): " + super.getHoverInfo(sourceViewer, lineNumber));
		}

		String hoverInfo = super.getHoverInfo(sourceViewer, lineNumber);
		//		String hoverInfo = "This is the hover info!!!";
		return hoverInfo == null ? "This is the hover info!" : hoverInfo;
	}

}
