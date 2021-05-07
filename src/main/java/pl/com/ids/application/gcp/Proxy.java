package pl.com.ids.application.gcp;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import java.util.Properties;

public class Proxy {
    private String hostName;
    private int port;
    private Credentials credentials;

    public Proxy(String host, String port) {
        this.hostName = host;
        this.port = Integer.parseInt(port);
    }

    public Proxy(Properties props) {
        this(props.getProperty("proxy_host"), props.getProperty("proxy_port"));
        String userName = props.getProperty("proxy_user");
        String password = props.getProperty("proxy_password");
        this.credentials = new UsernamePasswordCredentials(userName, password);

    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
