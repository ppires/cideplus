package cideplus.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import cideplus.ui.astview.ASTView;


public class PluginUtils {

	//private static Shell shell = null;
	private static IWorkbenchWindow workbenchWindow;

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
		final IWorkbench workbench = PlatformUI.getWorkbench();

		if (workbench != null) {

			// getActiveWorkbenchWindow() retorna null se não for chamado
			// de uma thread de UI.
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					workbenchWindow = workbench.getActiveWorkbenchWindow();
				}
			});

			if (workbenchWindow != null) {
				IWorkbenchPage page = workbenchWindow.getActivePage();
				if (page != null) {
					return page.getActiveEditor();
				}
				else {
					//					throw new RuntimeException("workbench window is null!");
				}
			}
		}
		else {
			//			throw new RuntimeException("workbench is null!");
		}
		return null;
	}

	/**
	 *  Get the editor that is currently being used, if it
	 *  is a text editor.
	 * 
	 * @returns The current text editor. <code>null</code> otherwise.
	 */
	public static ITextEditor getCurrentTextEditor() {
		IEditorPart editor = getCurrentEditor();
		if (editor instanceof ITextEditor) {
			return (ITextEditor) editor;
		}
		return null;
	}

	public static ASTView getASTView() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IWorkbenchPart part = page.getActivePart();
				if (part instanceof ASTView)
					return (ASTView)part;
			}
		}
		return null;
	}

	/**
	 * Get the project to which the currently opened file belongs to.
	 * 
	 * @return The current project, or <code>null</code> if there isn't any opened file.
	 */
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
		IFile file = getCurrentFile();
		if (file != null) {
			IJavaElement element = JavaCore.create(file);
			if (element instanceof ICompilationUnit)
				return (ICompilationUnit) element;
		}
		return null;
		//		//		IEditorInput inputElement = getCurrentEditor().getEditorInput();
		//		IJavaElement editorCU = EditorUtility.getActiveEditorJavaInput();
		//		if (editorCU instanceof ICompilationUnit) {
		//			//			System.out.println("First if");
		//			return (ICompilationUnit) editorCU;
		//		}
		//		else {
		//			editorCU = editorCU.getAncestor(IJavaElement.COMPILATION_UNIT);
		//			if (editorCU instanceof ICompilationUnit) {
		//				//				System.out.println("Second if");
		//				return (ICompilationUnit) editorCU;
		//			}
		//		}
	}


	/* mostra um popup com title e text */
	public static void showPopup(String title, String text) {
		MessageDialog.openInformation(getActiveShell(), title, text);
		JavaPlugin p;
	}

	/* Overloaded para colocar título default do popup */
	public static void showPopup(String text) {
		showPopup("CIDE+", text);
	}

	public static IDocument getCurrentDocument() {
		ITextEditor editor = getCurrentTextEditor();
		if (editor != null) {
			IDocumentProvider provider = editor.getDocumentProvider();
			if (provider != null) {
				return provider.getDocument(editor.getEditorInput());
			}
		}
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
