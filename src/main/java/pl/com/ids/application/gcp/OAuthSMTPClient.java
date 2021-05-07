package pl.com.ids.application.gcp;

import com.sun.mail.smtp.SMTPTransport;
import pl.com.ids.MimeMessageBuilder;
import pl.com.ids.infrastructure.Debugger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

class OAuthSMTPClient {
    void sendMessage(Debugger debugger, boolean debug, SMTPTransport smtpTransport, Properties sessionProps, String addressFrom, String addressTo, File f) throws MessagingException, IOException {
        Session session = Session.getInstance(sessionProps);
        String msgText1 = "Sending a file " + f.getName() + " .\n";
        String subject = "Sending a file " + f.getName();
        if (debug) {
            debugger.debug(subject);
        }
        MimeMessage msg = MimeMessageBuilder.buildMimeMessage(f, addressTo, addressFrom, msgText1, subject, session);
        smtpTransport.sendMessage(msg, new InternetAddress[]{new InternetAddress(addressTo)});
    }

    SMTPTransport connectToSmtp(String host,
                                int port,
                                String userEmail,
                                String oauthToken,
                                Proxy proxy,
                                boolean debug) throws MessagingException {
        // If the password is non-null, SMTP tries to do AUTH LOGIN.
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.sasl.enable", "true");
        props.put("mail.smtp.sasl.mechanisms", "XOAUTH2");

        setProxyInProps(proxy, props);

        props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
        // proxy with SMTP: https://community.oracle.com/tech/developers/discussion/1589188/javamail-send-a-mail-through-a-proxy-server-answer-and-with-gmail-ssl
        Session session = Session.getInstance(props);
        session.setDebug(debug);

        final URLName unusedUrlName = null;
        SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
        // If the password is non-null, SMTP tries to do AUTH LOGIN.
        final String emptyPassword = "";
        transport.connect(host, port, userEmail, emptyPassword);

        return transport;
    }

    private void setProxyInProps(Proxy proxy, Properties props) {
        if (proxy.getHostName() != null) {
            props.put("mail.smtp.proxy.host", proxy.getHostName());
            props.put("mail.smtp.proxy.port", String.valueOf(proxy.getPort()));
            props.put("mail.smtp.proxy.user", proxy.getCredentials().getUserPrincipal().getName());
            props.put("mail.smtp.proxy.password", proxy.getCredentials().getPassword());
        }
    }
}