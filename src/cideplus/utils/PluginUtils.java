package cideplus.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class PluginUtils {

	//private static Shell shell = null;

	/* retorna o workspace root */
	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/* retorna o shell sendo usado */
	public static Shell getDefaultShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/* retorna o editor sendo usado */
	public static IEditorPart getCurrentEditor() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}

	/* se o editor sendo usado for do tipo TextEditor, retorna ele. retorna null caso contrário */
	public static ITextEditor getCurrentTextEditor() {
		IEditorPart editor = getCurrentEditor();
		if (editor instanceof ITextEditor) {
			return (ITextEditor) editor;
		}
		else {
			return null;
		}
	}

	/* retorna o arquivo sendo editado */
	public static IFile getCurrentFile() {
		ITextEditor editor = getCurrentTextEditor();
		if (editor != null) {
			Object obj = editor.getEditorInput().getAdapter(IFile.class);
			if (obj != null) {
				return (IFile) obj;
			}
			else {
				showPopup("Não foi possível definir qual arquivo está sendo editado.");
				return null;
			}
		}
		return null;
	}

	/* retorna a current selection */
	public static ISelection getCurrentSelection() {
		ITextEditor editor = getCurrentTextEditor();
		if (editor != null) {
			return editor.getSelectionProvider().getSelection();
		}
		return null;
	}

	/* retorna a current selection se ela for uma TextSelection */
	public static ITextSelection getCurrentTextSelection() {
		ISelection selection = getCurrentSelection();
		if (selection instanceof ITextSelection) {
			return (ITextSelection) selection;
		}
		return null;
	}

	public static IDocument getCurrentDocument() {
		ITextEditor editor = getCurrentTextEditor();
		if (editor != null) {
			return editor.getDocumentProvider().getDocument(editor.getEditorInput());
		}
		return null;
	}

	/* mostra um popup com title e text */
	public static void showPopup(String title, String text) {
		MessageDialog.openInformation(getDefaultShell(), title, text);
	}

	/* Overloaded para colocar título default do popup */
	public static void showPopup(String text) {
		showPopup("CIDE+", text);
	}
}
