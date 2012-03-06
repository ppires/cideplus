package cideplus.ui.presentation;

import java.util.Iterator;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.IAnnotationHoverExtension2;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;

public class FeaturesAnnotationHover implements IAnnotationHover, IAnnotationHoverExtension, IAnnotationHoverExtension2 {

	public boolean canHandleMouseWheel() {
		// TODO Auto-generated method stub
		return false;
	}

	public IInformationControlCreator getHoverControlCreator() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean canHandleMouseCursor() {
		// TODO Auto-generated method stub
		return false;
	}

	public Object getHoverInfo(ISourceViewer sourceViewer, ILineRange lineRange, int visibleNumberOfLines) {
		return getHoverInfo(sourceViewer, lineRange.getStartLine());
	}

	public ILineRange getHoverLineRange(ISourceViewer viewer, int lineNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		IAnnotationModel model = sourceViewer.getAnnotationModel();
		Iterator it = model.getAnnotationIterator();
		Annotation annotation;
		while (it.hasNext()) {
			annotation = (Annotation) it.next();
			if (annotation.getType() == FeatureAnnotation.TYPE) {

			}
		}
		return "This is the hover info!";
	}

}
