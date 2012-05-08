package cideplus.ui.editor;

import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.rulers.AbstractContributedRulerColumn;

public class FeaturesRulerColumn extends AbstractContributedRulerColumn {

	IAnnotationModel annotationModel;
	Control control;

	@Override
	public void setModel(IAnnotationModel model) {
		AnnotationRulerColumn arc;
		annotationModel = model;
	}

	@Override
	public void redraw() {
		System.out.println("FeaturesRulerColumn.redraw()");
	}

	@Override
	public Control createControl(CompositeRuler parentRuler, Composite parentControl) {
		System.out.println("FeaturesRulerColumn.createControl()");
		control = parentControl;
		return parentControl;
		//		return null;
	}

	@Override
	public Control getControl() {
		System.out.println("FeaturesRulerColumn.getControl()");
		return control;
		//		return null;
	}

	@Override
	public int getWidth() {
		System.out.println("FeaturesRulerColumn.getWidth()");
		return 50;
	}

	@Override
	public void setFont(Font font) {
		System.out.println("FeaturesRulerColumn.setFont()");
	}

}
