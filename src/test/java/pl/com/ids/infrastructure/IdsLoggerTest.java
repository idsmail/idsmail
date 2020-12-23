package pl.com.ids.infrastructure;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IdsLoggerTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    IdsLogger idsLogger = new IdsLogger("in.txt") {
        @Override
        void doExit(int result) {
            // do nothing
        }
    };

    @Before
    public void setFolder() {
        idsLogger.setLogFolder(folder.getRoot());
    }

    @Test
    public void checkIfFileIsCreated() throws IOException {
        //when
        idsLogger.logFileNotFound();

        //then
        String fullname = FilenameUtils.concat(folder.getRoot().getAbsolutePath(), "in.txt");
        File output = new File(fullname);
        assertTrue(output.exists());
        assertEquals(FileUtils.readFileToString(output, StandardCharsets.UTF_8), "3");
    }
}