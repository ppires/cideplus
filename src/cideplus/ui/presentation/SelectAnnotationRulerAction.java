package cideplus.ui.presentation;

import java.util.ResourceBundle;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.IVerticalRulerInfoExtension;
import org.eclipse.jface.text.source.IVerticalRulerListener;
import org.eclipse.jface.text.source.VerticalRulerEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.TextEditorAction;

public class SelectAnnotationRulerAction extends TextEditorAction implements IVerticalRulerListener {

	/**
	 * Creates a new action for the given ruler and editor. The action configures
	 * its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction constructor), or  null if none
	 * @param editor the editor
	 *
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 */
	public SelectAnnotationRulerAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	/*
	 * @see org.eclipse.ui.texteditor.TextEditorAction#setEditor(org.eclipse.ui.texteditor.ITextEditor)
	 */
	@Override
	public void setEditor(ITextEditor editor) {
		if (getTextEditor() != null) {
			IVerticalRulerInfo service= (IVerticalRulerInfo) getTextEditor().getAdapter(IVerticalRulerInfo.class);
			if (service instanceof IVerticalRulerInfoExtension)
				((IVerticalRulerInfoExtension) service).removeVerticalRulerListener(this);
		}
		super.setEditor(editor);
		if (getTextEditor() != null) {
			IVerticalRulerInfo service= (IVerticalRulerInfo) getTextEditor().getAdapter(IVerticalRulerInfo.class);
			if (service instanceof IVerticalRulerInfoExtension)
				((IVerticalRulerInfoExtension) service).addVerticalRulerListener(this);
		}
	}

	/**
	 * Returns the <code>AbstractMarkerAnnotationModel of the editor's input.
	 *
	 * @return the marker annotation model or <code>null if there's none
	 */
	protected IAnnotationModel getAnnotationModel() {
		IDocumentProvider provider= getTextEditor().getDocumentProvider();
		return provider.getAnnotationModel(getTextEditor().getEditorInput());
	}

	/*
	 * @see org.eclipse.ui.texteditor.IVerticalRulerListener#annotationSelected(org.eclipse.ui.texteditor.VerticalRulerEvent)
	 */
	public void annotationSelected(VerticalRulerEvent event) {
	}

	/*
	 * @see org.eclipse.ui.texteditor.IVerticalRulerListener#annotationDefaultSelected(org.eclipse.ui.texteditor.VerticalRulerEvent)
	 */
	public void annotationDefaultSelected(VerticalRulerEvent event) {
		Annotation a= event.getSelectedAnnotation();
		IAnnotationModel model= getAnnotationModel();
		Position position= model.getPosition(a);
		if (position == null)
			return;

		getTextEditor().selectAndReveal(position.offset, position.length);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IVerticalRulerListener#annotationContextMenuAboutToShow(org.eclipse.ui.texteditor.VerticalRulerEvent, org.eclipse.swt.widgets.Menu)
	 */
	public void annotationContextMenuAboutToShow(VerticalRulerEvent event, Menu menu) {
	}








	//	public SelectAnnotationRulerAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
	//		super(bundle, prefix, editor);
	//	}
	//
	//	public SelectAnnotationRulerAction(ResourceBundle bundle, String prefix, ITextEditor editor, int style) {
	//		super(bundle, prefix, editor, style);
	//	}
	//
	//	public void annotationSelected(VerticalRulerEvent event) {
	//
	//	}
	//
	//	public void annotationDefaultSelected(VerticalRulerEvent event) {
	//
	//	}
	//
	//	public void annotationContextMenuAboutToShow(VerticalRulerEvent event, Menu menu) {
	//
	//	}

}
