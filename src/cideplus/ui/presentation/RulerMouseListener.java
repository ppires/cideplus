package cideplus.ui.presentation;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.ui.IEditorPart;

import cideplus.ui.editor.EditorListener;

public class RulerMouseListener implements MouseListener {

	private IEditorPart editor;
	private ISourceViewer viewer;
	private IVerticalRulerInfo ruler_info;
	private int line_number;

	public RulerMouseListener() {
		super();
		AnnotationRulerColumn a;
		System.out.println("RulerMouseListener.RulerMouseListener()");
	}

	public RulerMouseListener(IEditorPart editor) {
		System.out.println("RulerMouseListener.RulerMouseListener(Control)");
		this.editor = editor;
		Object obj = editor.getAdapter(IVerticalRulerInfo.class);
		if (obj != null && obj instanceof IVerticalRulerInfo) {
			ruler_info = (IVerticalRulerInfo) obj;
		}
		if (editor instanceof JavaEditor) {
			viewer = ((JavaEditor) editor).getViewer();
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		System.out.println("RulerMouseListener.mouseDoubleClick()");
	}

	@Override
	public void mouseDown(MouseEvent e) {
		System.out.println("RulerMouseListener.mouseDown()");
		line_number = ruler_info.toDocumentLineNumber(e.y) + 1;
	}

	@Override
	public void mouseUp(MouseEvent e) {
		System.out.println("RulerMouseListener.mouseUp()");
		line_number = ruler_info.toDocumentLineNumber(e.y) + 1;
		System.out.println("line clicked: " + line_number);
		IPainter painter = EditorListener.getPainter(viewer);
		if (painter instanceof CustomAnnotationPainter) {
			((CustomAnnotationPainter) painter).setAnnotationToPaint(line_number);
		}
		else {
			System.out.println("found painter that is NOT CustomAnnotationPainter!");
		}
	}

}
