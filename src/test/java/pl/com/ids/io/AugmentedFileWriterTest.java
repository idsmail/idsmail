package pl.com.ids.io;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static junit.framework.Assert.*;

public class AugmentedFileWriterTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

	private AugmentedFileWriter testedWriter = new AugmentedFileWriter(false, true);

	@Test
	public void shouldReturnNewFileName() {
		String actual = testedWriter.appendNumberToBaseFileName("c:\\karol\\as.txt", 2);
		assertEquals("c:\\karol\\as(2).txt", actual);
	}
	
	@Test
	public void shouldReturnUniqueFileName() throws IOException {
        File newFile = folder.newFile("as.txt");
		File actual = testedWriter.getUniqueFileName(newFile);
		assertEquals(actual.getName(), "as(1).txt");

	}

}
