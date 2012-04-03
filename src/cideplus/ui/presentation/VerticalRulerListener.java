package cideplus.ui.presentation;

import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.text.source.VerticalRulerEvent;
import org.eclipse.swt.widgets.Menu;

public class VerticalRulerListener implements IVerticalRulerListener {

	public VerticalRulerListener() {
		super();
		System.out.println("VerticalRulerListener()");
	}

	public void annotationSelected(VerticalRulerEvent event) {
		System.out.println("annotationSelected()");
	}

	public void annotationDefaultSelected(VerticalRulerEvent event) {
		System.out.println("annotationDefaultSelected()");
	}

	public void annotationContextMenuAboutToShow(VerticalRulerEvent event, Menu menu) {
		System.out.println("annotationContextMenuAboutToShow()");
	}

}
