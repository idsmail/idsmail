package pl.com.ids.application.gcp;

import com.sun.mail.smtp.SMTPTransport;
import pl.com.ids.infrastructure.Debugger;
import pl.com.ids.infrastructure.DebuggerFactory;
import pl.com.ids.infrastructure.IdsLogger;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.Properties;

import static pl.com.ids.application.gcp.SendOptionsProcessor.*;

public class SendXMLGoogle {


    public static void initialize() {
        Security.addProvider(new FetchXMLGoogle.OAuth2Provider());
    }

    public static void main(String[] args) throws IOException {
        IdsLogger log = new IdsLogger("out.txt");
        Debugger debugger = DebuggerFactory.getDebugger(args);

        initialize();
        Properties props = new SendOptionsProcessor().getSendOptions(args, log, debugger);
        boolean debug = Boolean.parseBoolean(props.getProperty(DEBUG));
        OAuthClient oAuthClient = new OAuthClient(debug);

        String accessToken = oAuthClient.refreshToken(props.getProperty("oauth.client_id"),
                props.getProperty("oauth.client_secret"),
                props.getProperty("oauth.refresh_token"));
        String emailAddress = props.getProperty("email_address");

        try {
            final OAuthSMTPClient oAuthSMTPClient = new OAuthSMTPClient();
            SMTPTransport smtpTransport = oAuthSMTPClient.connectToSmtp("smtp.gmail.com", 587, emailAddress, accessToken, debug);
            Properties sessionProps = new Properties();
            String addressFrom = props.getProperty(ADDRESS_FROM);
            String addressTo = props.getProperty(ADDRESS_TO);
            File f = new File(props.getProperty(FILE_NAME));
            oAuthSMTPClient.sendMessage(debugger, debug, smtpTransport, sessionProps, addressFrom, addressTo, f);
            log.logSuccess();
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
    }

}
