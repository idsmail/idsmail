package pl.com.ids.application.gcp;

import com.sun.mail.smtp.SMTPTransport;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.Security;
import java.util.Properties;

import static pl.com.ids.MimeMessageBuilder.buildMimeMessage;

public class SendXMLGoogle {
    public static void initialize() {
        Security.addProvider(new FetchXMLGoogle.OAuth2Provider());
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            printUsage();
            System.exit(3);
        }

        initialize();
        Properties props = new Properties();
        try {
            props.load(new FileReader(new File("app.cfg")));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
        OAuthClient oAuthClient = new OAuthClient();
        String accessToken = oAuthClient.refreshToken(props.getProperty("oauth.client_id"),
                props.getProperty("oauth.client_secret"),
                props.getProperty("oauth.refresh_token"));
        String emailAddress = props.getProperty("email_address");

        try {
            SMTPTransport smtpTransport = new SendXMLGoogle().connectToSmtp("smtp.gmail.com",
                    587,
                    emailAddress,
                    accessToken,
                    true);
            Properties sessionProps = new Properties();
            Session session = Session.getInstance(sessionProps);
            String addressTo = args[2];
            File f = new File(args[0]);
            String msgText1 = "Sending a file " + f.getName() + " .\n";
            String subject = "Sending a file " + f.getName();
            MimeMessage msg = buildMimeMessage(f, args[1], addressTo, msgText1, subject, session);
            smtpTransport.sendMessage(msg, new InternetAddress[]{new InternetAddress(addressTo)});

        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.err
                .println("Usage: sendXML file_name address_to address_from");
    }

    private SMTPTransport connectToSmtp(String host,
                                               int port,
                                               String userEmail,
                                               String oauthToken,
                                               boolean debug) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.sasl.enable", "true");
        props.put("mail.smtp.sasl.mechanisms", "XOAUTH2");
        props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
        Session session = Session.getInstance(props);
        session.setDebug(debug);

        final URLName unusedUrlName = null;
        SMTPTransport transport = new SMTPTransport(session, unusedUrlName);
        // If the password is non-null, SMTP tries to do AUTH LOGIN.
        final String emptyPassword = "";
        transport.connect(host, port, userEmail, emptyPassword);

        return transport;
    }

}
