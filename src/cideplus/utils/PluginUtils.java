package cideplus.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;


public class PluginUtils {

	//private static Shell shell = null;

	private PluginUtils() {

	}

	/* retorna o workspace root */
	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/* retorna o shell sendo usado */
	public static Shell getActiveShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/* retorna o editor sendo usado */
	public static IEditorPart getCurrentEditor() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				return page.getActiveEditor();
			}
		}
		return null;
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

	public static IProject getCurrentProject() {
		IFile file = getCurrentFile();
		if (file != null) {
			return file.getProject();
		}
		return null;
	}

	public static IJavaProject getCurrentJavaProject() {
		IProject project = getCurrentProject();
		return JavaCore.create(project);
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

	/* retorna a current selection do editor */
	public static ISelection getCurrentEditorSelection() {
		ITextEditor editor = getCurrentTextEditor();
		if (editor != null) {
			return editor.getSelectionProvider().getSelection();
		}
		return null;
	}

	/* Retorna a current selection */
	public static ISelection getCurrentSelection() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
	}

	/* retorna o texto selecionado no editor. */
	public static ITextSelection getCurrentEditorTextSelection() {
		ISelection selection = getCurrentEditorSelection();
		if (selection instanceof ITextSelection) {
			return (ITextSelection) selection;
		}
		return null;
	}


	public static ICompilationUnit getCurrentCompilationUnit() {
		//		IEditorInput inputElement = getCurrentEditor().getEditorInput();
		IJavaElement editorCU = EditorUtility.getActiveEditorJavaInput();
		if (editorCU instanceof ICompilationUnit) {
			//			System.out.println("First if");
			return (ICompilationUnit) editorCU;
		}
		else {
			editorCU = editorCU.getAncestor(IJavaElement.COMPILATION_UNIT);
			if (editorCU instanceof ICompilationUnit) {
				//				System.out.println("Second if");
				return (ICompilationUnit) editorCU;
			}
		}
		return null;
	}


	/* mostra um popup com title e text */
	public static void showPopup(String title, String text) {
		MessageDialog.openInformation(getActiveShell(), title, text);
	}

	/* Overloaded para colocar título default do popup */
	public static void showPopup(String text) {
		showPopup("CIDE+", text);
	}

	public static IDocument getCurrentDocument() {
		ITextEditor editor = getCurrentTextEditor();
		if (editor != null)
			return editor.getDocumentProvider().getDocument(editor.getEditorInput());

		return null;
	}

	public static IAnnotationModel getAnnotationsModel(ITextEditor editor) {
		if (editor == null)
			editor = getCurrentTextEditor();

		if (editor != null)
			return editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());

		return null;
	}

}
