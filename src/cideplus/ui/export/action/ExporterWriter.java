package cideplus.ui.export.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

public class ExporterWriter {

	public void writeFilesToDir(File dir, Map<String, byte[]> exportedFiles, IProgressMonitor monitor) throws IOException {
		Set<String> keySet = exportedFiles.keySet();		
		for (String fullFileName : keySet) {
			byte[] bytesToWrite = exportedFiles.get(fullFileName);
			monitor.setTaskName("Writing... "+fullFileName);
			monitor.worked(1);
			if(bytesToWrite.length == 0){
				continue;
			}
			File fileDir = new File(dir, getDir(fullFileName));
			if(!fileDir.exists()){
				fileDir.mkdirs();
			}
			File file = new File(fileDir, getFile(fullFileName));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			try {
				out.write(bytesToWrite);
				out.flush();
			} finally {
				out.close();
			}
		}
	}

	private String getFile(String fullFileName) {
		return fullFileName.substring(fullFileName.lastIndexOf('/') + 1);
	}

	private String getDir(String fullFileName) {
		return fullFileName.substring(0, fullFileName.lastIndexOf('/'));
	}

}
