package cideplus.ui.export.action;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

import cideplus.FeaturerPlugin;
import cideplus.model.Feature;
import cideplus.ui.editor.FeaturerCompilationUnitEditor;
import cideplus.ui.export.dialog.ExportFileDialog;
import cideplus.ui.export.dialog.SelectFeaturesDialog;
import cideplus.utils.PluginUtils;

public class ExportProjectAction implements IObjectActionDelegate {

	private Shell shell;
	private IJavaProject project;

	public void run(IAction action) {
		ITextEditor editor = PluginUtils.getCurrentTextEditor();
		if (editor instanceof FeaturerCompilationUnitEditor) {
			((FeaturerCompilationUnitEditor) editor).getColorPresentation().refreshFeatures();
		}
		try {
			ExportFileDialog dialog = new ExportFileDialog(shell);
			File exportFile = dialog.getExportFile();
			if(exportFile != null){
				if(exportFile.isDirectory()){
					SelectFeaturesDialog selectFeatures = new SelectFeaturesDialog(shell, project);
					Set<Feature> features = selectFeatures.selectFeatures();
					System.out.println(features);
					if(features != null){
						//exportando para um diretorio
						doExport(exportFile, features);
					}
				} else if(exportFile.isFile()){
					//exportando para arquvio
					MessageDialog.openWarning(shell, "Export Project", "Exporting to files is not supported yet.");
				}
			}
		} catch (Exception e) {
			MessageDialog.openError(shell, "Error", e.getMessage());
			e.printStackTrace();
		}
	}

	private void doExport(final File exportFile, final Set<Feature> features) {
		WorkspaceJob job = new WorkspaceJob("Export project") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					Exporter exporter = new Exporter(shell, project, features);
					int fileCount = exporter.getFileCount(monitor);
					//o fileCount � multiplicado por 4 para passar ao monitor pois
					//o m�todo getExportedFiles tem peso 3
					//e o m�todo writeFilesToDir tem peso 1
					monitor.beginTask("Exporting "+fileCount+" files", fileCount * 4);
					Map<String, byte[]> exportedFiles = exporter.getExportedFiles(monitor);
					ExporterWriter writer = new ExporterWriter();
					writer.writeFilesToDir(exportFile, exportedFiles, monitor);
					monitor.done();
				} catch (OperationCanceledException e) {
					return new Status(IStatus.CANCEL, FeaturerPlugin.PLUGIN_ID,	e.getMessage());
				} catch (Exception e) {
					return new Status(IStatus.ERROR, FeaturerPlugin.PLUGIN_ID, e.getMessage());
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IJavaProject){
			project = (IJavaProject) selection;
		} else if (selection instanceof IStructuredSelection){
			project = (IJavaProject) ((IStructuredSelection)selection).getFirstElement();
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

}
