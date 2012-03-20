package cideplus.ui.presentation;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;

import cideplus.FeaturerPlugin;
import cideplus.utils.PluginUtils;

public class FeaturesPainter implements ITextPresentationListener, IPainter, PaintListener {

	/**
	 * Cache responsible for managing the styles to be
	 * applied on text.
	 */
	private StyleCache styleCache;

	/**
	 * Flag indicating if this painter is active.
	 */
	private boolean isActive = false;

	/**
	 * The sourceViewer
	 */
	ISourceViewer sourceViewer;

	/**
	 * The text widget of the source viewer.
	 */
	StyledText textWidget;


	public FeaturesPainter(ISourceViewer sourceViewer) {
		styleCache = StyleCache.getInstance();
		this.sourceViewer = sourceViewer;
		textWidget = sourceViewer.getTextWidget();
	}

	/**
	 * Enables this painter.
	 */
	private void enablePainting() {
		if (!isActive)
			isActive = true;
		textWidget.addPaintListener(this);
	}

	/**
	 * Disables this painter.
	 */
	private void disablePainting() {
		textWidget.removePaintListener(this);
		if (isActive)
			isActive = false;
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

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void paint(int reason) {
		if (sourceViewer.getDocument() == null) {
			deactivate(false);
			return;
		}

		if (!isActive) {
			enablePainting();
		}
		else if (isRepaintReason(reason)) {
			sourceViewer.invalidateTextPresentation();
			//			updatePainting(null);
		}

	}

	public void deactivate(boolean redraw) {
		isActive = false;
		disablePainting();
	}

	public void setPositionManager(IPaintPositionManager manager) {
		// TODO Auto-generated method stub
	}

	public void paintControl(PaintEvent e) {
		// TODO Auto-generated method stub
	}


	/**
	 * Returns whether the given reason causes a repaint.
	 *
	 * @param reason the reason
	 * @return <code>true</code> if repaint reason, <code>false</code> otherwise
	 * @since 3.0
	 */
	protected boolean isRepaintReason(int reason) {
		return IPainter.TEXT_CHANGE == reason;
		//		return CONFIGURATION == reason || INTERNAL == reason;
	}
}

