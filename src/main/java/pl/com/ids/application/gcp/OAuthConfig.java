package pl.com.ids.application.gcp;

import java.io.File;

public class OAuthConfig {

    private File oauthConfig;

    public OAuthConfig(File oauthConfig) {
        this.oauthConfig = oauthConfig;
    }

    public File getOauthConfig() {
        return oauthConfig;
    }
}
