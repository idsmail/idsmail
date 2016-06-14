package pl.com.ids;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class StatusNotifier {
	private final String FILENAME = "status.txt";
	private final File diskFolder;
	
	public StatusNotifier(File diskFolder) {
		this.diskFolder = diskFolder;
	}
	
	public void updateStatus(ProcessingStatus status) {
		try {
			storeStatus(status);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void storeStatus(ProcessingStatus status)
			throws IOException {
		try {

				File f = new File(diskFolder.getAbsolutePath(), FILENAME);
				saveFile(f, status.getValue());
			

		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe);
		}
	}

	private void saveFile(File f, String content) throws FileNotFoundException,
			IOException {
		byte[] buf = content.getBytes();

		FileOutputStream out = new FileOutputStream(f);
		out.write(buf, 0, buf.length);
		out.close();
	}
}
