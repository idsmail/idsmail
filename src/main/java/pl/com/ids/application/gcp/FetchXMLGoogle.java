package pl.com.ids.application.gcp;

import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.imap.IMAPStore;
import pl.com.ids.FetchOptions;
import pl.com.ids.FetchXML;
import pl.com.ids.SimpleDebugger;
import pl.com.ids.io.AugmentedFileWriter;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;

public class FetchXMLGoogle {
    public static final class OAuth2Provider extends Provider {
        private static final long serialVersionUID = 1L;

        public OAuth2Provider() {
            super("Google OAuth2 Provider", 1.0,
                    "Provides the XOAUTH2 SASL Mechanism");
            put("SaslClientFactory.XOAUTH2",
                    "pl.com.ids.application.gcp.OAuth2SaslClientFactory");
        }
    }

    public static void main(String[] args) throws IOException {
        SimpleDebugger debugger = new SimpleDebugger();
        OptionsFetcher optionsFetcher = new OptionsFetcher(args);
        FetchOptions fetchOptions = optionsFetcher.getFetchOptions(debugger);

        initialize();
        try {
            Properties props = new Properties();
            props.load(new FileReader(new File("app.cfg")));
            System.out.println(props.getProperty("oauth.refresh_token"));
            OAuthClient oAuthClient = new OAuthClient();
            String accessToken = oAuthClient.refreshToken(props.getProperty("oauth.client_id"),
                    props.getProperty("oauth.client_secret"),
                    props.getProperty("oauth.refresh_token"));
            String email_address = props.getProperty("email_address");

            IMAPStore imapStore = new FetchXMLGoogle().connectToImap("imap.gmail.com",
                    993,
                    email_address,
                    accessToken,
                    true);
            FetchXML fetchXML = new FetchXML(debugger);
            AugmentedFileWriter fileWriter = new AugmentedFileWriter(fetchOptions.getOverwrite(), fetchOptions.isDebug());
            Folder inbox = imapStore.getFolder("INBOX");
            fetchXML.processFolder(fetchOptions, fileWriter, inbox);
        } catch (IOException | MessagingException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Installs the OAuth2 SASL provider. This must be called exactly once before
     * calling other methods on this class.
     */
    public static void initialize() {
        Security.addProvider(new OAuth2Provider());
    }


    /**
     * Connects and authenticates to an IMAP server with OAuth2. You must have
     * called {@code initialize}.
     *
     * @param host       Hostname of the imap server, for example {@code
     *                   imap.googlemail.com}.
     * @param port       Port of the imap server, for example 993.
     * @param userEmail  Email address of the user to authenticate, for example
     *                   {@code oauth@gmail.com}.
     * @param oauthToken The user's OAuth token.
     * @param debug      Whether to enable debug logging on the IMAP connection.
     * @return An authenticated IMAPStore that can be used for IMAP operations.
     */
    public IMAPStore connectToImap(String host,
                                   int port,
                                   String userEmail,
                                   String oauthToken,
                                   boolean debug) throws MessagingException {
        System.out.println(userEmail);
        System.out.println(oauthToken);
        Properties props = new Properties();
        props.put("mail.imaps.sasl.enable", "true");
        props.put("mail.imaps.sasl.mechanisms", "XOAUTH2");
        props.put(OAuth2SaslClientFactory.OAUTH_TOKEN_PROP, oauthToken);
        Session session = Session.getInstance(props);
        session.setDebug(debug);

        final URLName unusedUrlName = null;
        IMAPSSLStore store = new IMAPSSLStore(session, unusedUrlName);
        final String emptyPassword = "";
        store.connect(host, port, userEmail, emptyPassword);
        return store;
    }
}
