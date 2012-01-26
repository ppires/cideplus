package cideplus.ui.export.dialog;

import java.io.File;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class ExportFileDialog {

	private Shell shell;

	public ExportFileDialog(Shell parent) {
		this.shell = parent;
	}
	
	public File getExportFile(){
		DirectoryDialog dialog = new DirectoryDialog(shell);
		dialog.setText("Export Project Directory");
		dialog.setMessage("Choose a directory to export the project");
		String dir = dialog.open();
		if(dir != null){
			return new File(dir);
		}
		return null;
	}

}
