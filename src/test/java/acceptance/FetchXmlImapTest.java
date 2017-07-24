package acceptance;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import pl.com.ids.*;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.Optional;

public class FetchXmlImapTest {


    public static final String USER = "user@greenmail.com";
    public static final String PASSWORD = "password";
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.IMAPS);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory.class.getName());
        greenMail.start();
    }

    @After
    public void tearDown() {
        greenMail.stop();
    }

    @Test
    public void fetchMail() throws IOException, MessagingException {
        GreenMailUser greenMailUser = greenMail.setUser(USER, PASSWORD);
        File f = temporaryFolder.newFile("attached.xml");
        ServerSetup serverSetup = greenMail.getImaps().getServerSetup();
        Session session = GreenMailUtil.getSession(serverSetup);
        MimeMessage mimeMessage = MimeMessageBuilder.buildMimeMessage(f, "test@mail.com", "user@greenmail.com", "messageText", "subject", session);
        greenMailUser.deliver(mimeMessage);
        FetchXML fetchXML = new FetchXML(new NoOpDebugger());
        File diskFolder = temporaryFolder.newFolder();
        Service service = new Service("127.0.0.1", serverSetup.getPort(), USER, PASSWORD, Protocol.IMAPS, false);
        fetchXML.fetch(new FetchOptions(service, diskFolder, USER, "20-02-2016", "30-10-2017", true, true, true, Optional.of(50000), false));

        Assert.assertArrayEquals(new String[]{"attached.xml", "status.txt"}, diskFolder.list());
    }
}
