package cideplus.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import cideplus.ui.astview.ASTView;


public class PluginUtils {

	/**
	 * workbench window estático para poder ser atribuído de
	 * uma thread de UI.
	 */
	private static IWorkbenchWindow workbenchWindow;

	/**
	 * Usado para retornar o project em getCurrentSelectedProject
	 */
	private static IProject project;

	/**
	 * usado para retorna ISelection em getCurrentSelection
	 */
	private static ISelection selection;

	/**
	 * ID do Package Explorer
	 */
	private static final String PEXLORER_ID = "org.eclipse.jdt.ui.PackageExplorer";


	private PluginUtils() {
	}


	/* retorna o workspace root */
	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}


	/* retorna o shell sendo usado */
	public static Shell getActiveShell() {
		return Display.getDefault().getActiveShell();
	}


	/**
	 * retorna o editor sendo usado
	 */
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
				showPopup("Não foi possível definir qual arquivo está aberto no editor.");
				return null;
			}
		}
		return null;
	}



	/* Retorna a current selection */
	public static ISelection getCurrentSelection() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			// getActiveWorkbenchWindow() retorna null se não for chamado
			// de uma thread de UI.
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					workbenchWindow = workbench.getActiveWorkbenchWindow();
					if (workbenchWindow != null) {
						ISelectionService service = workbenchWindow.getSelectionService();
						selection = service.getSelection();
					}
					else {
						System.out.println("\n\nwindow is NULL even in a UI thread!");
					}
				}
			});
		}
		else {
			workbenchWindow = null;
		}

		return selection;
	}


	/**
	 * Get the project selected in the package explorer
	 * @return the project
	 */
	public static IProject getCurrentSelectedProject() {
		ISelection selection = getCurrentSelection();
		if (selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			//				System.out.println("\n\nselection first element class: " + obj.getClass());
			if (obj instanceof IProject) {
				return (IProject) obj;
			}
			else if (obj instanceof IJavaProject) {
				return ((IJavaProject) obj).getProject();
			}
			else {
				System.out.println("\n\nobject isn't IProject nor IJavaProject!");
			}
		}
		else {
			System.out.println("\n\nselection isn't structured selection!");
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


	/* retorna a current selection do editor como um ITextSelection */
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
	}


	/* mostra um popup com title e text */
	public static void showPopup(String title, String text) {
		MessageDialog.openInformation(getActiveShell(), title, text);
	}


	/* Overloaded para colocar título default do popup */
	public static void showPopup(String text) {
		showPopup("CIDE+", text);
	}


	/**
	 * 
	 */
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


	/**
	 * retorna o IAnnotationModel associado a um editor.
	 * Se o editor passado com parâmetro for null, retorna
	 * o IAnnotationModel do editor ativo no momento. Retorna
	 * null se não conseguir pegar o model.
	 */
	public static IAnnotationModel getAnnotationsModel(ITextEditor editor) {
		if (editor == null)
			editor = getCurrentTextEditor();

		if (editor != null)
			return editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());

		return null;
	}


	/**
	 * Retorna o ISourceViewer associado ao editor aberto no momento.
	 */
	public static ISourceViewer getCurrentSourceViewer() {
		IEditorPart editor = getCurrentEditor();
		if (editor != null && editor instanceof JavaEditor)
			return ((JavaEditor) editor).getViewer();
		return null;
	}

}
