package pl.com.ids;

import java.io.File;

public class SendOptions {
    private final File f;
    private final Boolean auth;
    private final String username;
    private final String password;
    private final Logger l;
    private final Boolean ssl;
    private final Boolean tls;
    private final Integer timeout;

    public SendOptions(File f, Boolean auth, String username, String password, Logger l, Boolean ssl, Boolean tls, Integer timeout) {
        this.f = f;
        this.auth = auth;
        this.username = username;
        this.password = password;
        this.l = l;
        this.ssl = ssl;
        this.tls = tls;
        this.timeout = timeout;
    }

    public File getF() {
        return f;
    }

    public Boolean getAuth() {
        return auth;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Logger getL() {
        return l;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public Boolean getTls() {
        return tls;
    }

    public Integer getTimeout() {
        return timeout;
    }
}
