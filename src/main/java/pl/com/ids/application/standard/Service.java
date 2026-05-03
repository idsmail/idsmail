package pl.com.ids.application.standard;

import pl.com.ids.Protocol;

import jakarta.mail.URLName;

public class Service {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final Protocol protocol;
    private final Boolean useTls;

    public Protocol getProtocol() {
        return protocol;
    }

    public Service(String host, int port, String username, String password, Protocol protocol, Boolean useTls) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.protocol = protocol;
        this.useTls = useTls;
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }


    public URLName getUrl() {
        return new URLName(protocol.getCode() , host, port, getFolder(),  username , password);
    }

    private String getFolder() {
        return "INBOX";
    }


    public Boolean getUseTls() {
        return useTls;
    }
}
