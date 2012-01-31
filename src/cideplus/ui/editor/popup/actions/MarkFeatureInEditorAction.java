package cideplus.ui.editor.popup.actions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import cideplus.ui.presentation.FeaturesMarkerFactory;
import cideplus.utils.PluginUtils;

public class MarkFeatureInEditorAction implements IEditorActionDelegate {

	//	private Shell shell;
	//	private ITextSelection selection;

	/**
	 * Constructor for Action1.
	 */
	public MarkFeatureInEditorAction() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		ITextSelection selection = PluginUtils.getCurrentTextSelection();
		System.out.println("offset: " + selection.getOffset() + " length: " + selection.getLength());
		try {
			IResource resource = PluginUtils.getCurrentFile();
			FeaturesMarkerFactory.createMarker(resource, selection, 1);
		} catch (CoreException e) {
			System.out.println("Caught Exception creating marker!!!");
			e.printStackTrace();
		}

		System.out.println("-----");
		for(IMarker marker: FeaturesMarkerFactory.findAllMarkers()) {
			FeaturesMarkerFactory.printMarker(marker);
			System.out.println(System.getProperty("line.separator"));
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		//		System.out.println("selectionChanged()");
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		//		System.out.println("setActiveEditor()");
	}
}
