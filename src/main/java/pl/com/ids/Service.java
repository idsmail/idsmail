package pl.com.ids;

import javax.mail.URLName;

public class Service {
    private final String host;
    private int port;
    private final String username;
    private final String password;
    private Protocol protocol;
    private Boolean useTls;

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

    String getHost() {
        return host;
    }

    String getUsername() {
        return username;
    }


    URLName getUrl() {
        return new URLName(protocol.getCode() , host, port, getFolder(),  username , password);
    }

    private String getFolder() {
        return "INBOX";
    }


    public Boolean getUseTls() {
        return useTls;
    }
}
