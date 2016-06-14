package pl.com.ids.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;

public class AugmentedFileWriter {
	final Boolean overwrite;
	final boolean debug;
	public AugmentedFileWriter(Boolean overwrite, boolean debug) {
		this.overwrite = overwrite;
		this.debug = debug;
	}
	
	protected File getUniqueFileName(File fileName) {
		int idx = 1;
		File result = fileName;
		
		while (result.exists()) {
			result = new File(appendNumberToBaseFileName(fileName.getAbsolutePath(), idx));
			idx++;
		}
		
		return result; 
	}
	
	protected String appendNumberToBaseFileName(String fullFileName, int index) {
		final String fullPath = FilenameUtils.getFullPath(fullFileName);
		final String base = FilenameUtils.getBaseName(fullFileName);
		final String extension = FilenameUtils.getExtension(fullFileName);
		
		final String newFileName = base + "(" + index + ")" + "." + extension;
		final String newFullFileName = FilenameUtils.concat(fullPath, newFileName);
		return newFullFileName;
		
	}
	
	public void storeFile(File diskFolder, String fileName, InputStream in) throws IOException {
		try {

			if (fileName == null)
				return;
			File f = new File(diskFolder.getAbsolutePath(), fileName);

			if (overwrite != null && overwrite.booleanValue()) {
				saveFile(in, f);
			} else {
				File outputFile = getUniqueFileName(f);
				saveFile(in, outputFile);
				
			}

		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe);
		}
	}

	private void saveFile(InputStream in, File f) throws FileNotFoundException,
			IOException {
		int bufSize = 1024;
		int amountRead = 0;
		byte[] buf = new byte[1024];

		FileOutputStream out = new FileOutputStream(f);
		while ((amountRead = in.read(buf, 0, bufSize)) != -1) {
			out.write(buf, 0, amountRead);
		}
		if (debug)
			System.out.println("Nadpisuje");
	}

}
