package cideplus.ui.editor.popup.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import cideplus.ui.presentation.FeaturesMarkerFactory;
import cideplus.utils.PluginUtils;

public class MarkFeatureInEditorAction implements IWorkbenchWindowActionDelegate {

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
		ITextSelection selection = PluginUtils.getCurrentEditorTextSelection();
		try {
			IResource resource = PluginUtils.getCurrentFile();
			FeaturesMarkerFactory.createMarker(resource, selection, 1);
		} catch (CoreException e) {
			System.out.println("Caught Exception creating marker!!!");
			e.printStackTrace();
		}

		//		FeaturesMarkerFactory.printAllMarkers();
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

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}
}
