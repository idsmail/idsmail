package pl.com.ids.application.gcp;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import pl.com.ids.domain.Configuration;
import pl.com.ids.infrastructure.IdsLogger;

import java.io.File;
import java.io.IOException;

public class OptionsFetcherTest {
    private final IdsLogger idsLogger = new IdsLogger("in.txt");
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void configFileIsReadProperly() throws IOException {
        File cfg = temporaryFolder.newFile();
        String folder = String.valueOf(temporaryFolder.getRoot());
        OptionsFetcher optionsFetcher = new OptionsFetcher(new String[]{folder, "-c", cfg.getAbsolutePath()});

        Configuration<OAuthConfig> fetchOptions = optionsFetcher.getFetchOptions(idsLogger, null);
        Assert.assertTrue(fetchOptions.getSpecific().getOauthConfig().exists());
    }

    @Test
    public void configFileDefaultValue() throws IOException {
        File cfg = temporaryFolder.newFile();
        String folder = String.valueOf(temporaryFolder.getRoot());
        OptionsFetcher optionsFetcher = new OptionsFetcher(new String[]{folder});
        Configuration<OAuthConfig> fetchOptions = optionsFetcher.getFetchOptions(idsLogger, null);
        Assert.assertEquals(fetchOptions.getSpecific().getOauthConfig().getName(), "app.cfg");
    }


}