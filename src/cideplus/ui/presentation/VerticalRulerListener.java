package cideplus.ui.presentation;

import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.text.source.VerticalRulerEvent;
import org.eclipse.swt.widgets.Menu;

public class VerticalRulerListener implements IVerticalRulerListener {

	public VerticalRulerListener() {
		//		org.eclipse.ui.texteditor.SelectAnnotationRulerAction s;
		System.out.println("VerticalRulerListener.VerticalRulerListener()");
	}

	@Override
	public void annotationSelected(VerticalRulerEvent event) {
		System.out.println("VerticalRulerListener.annotationSelected()");
	}

	@Override
	public void annotationDefaultSelected(VerticalRulerEvent event) {
		System.out.println("VerticalRulerListener.annotationDefaultSelected()");
	}

	@Override
	public void annotationContextMenuAboutToShow(VerticalRulerEvent event, Menu menu) {
		System.out.println("VerticalRulerListener.annotationContextMenuAboutToShow()");
	}

}
