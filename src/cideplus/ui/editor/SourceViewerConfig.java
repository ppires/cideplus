package cideplus.ui.editor;

import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

import cideplus.ui.presentation.FeaturesAnnotationHover;

public class SourceViewerConfig extends JavaSourceViewerConfiguration {

	public SourceViewerConfig(IColorManager colorManager, IPreferenceStore preferenceStore, ITextEditor editor, String partitioning) {
		super(colorManager, preferenceStore, editor, partitioning);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		System.out.println("SourceViewerConfig.getAnnotationHover()");
		return new FeaturesAnnotationHover();
	}
}
