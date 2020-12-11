package acceptance;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import pl.com.ids.MimeMessageBuilder;
import pl.com.ids.SendOptions;
import pl.com.ids.application.standard.SendXML;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;

public class SendXmlTest {
    public static final String USER = "user@greenmail.com";
    public static final String PASSWORD = "password";
    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(IdsServerSetup.extendTo(ServerSetup.SMTP));

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        greenMail.start();
    }

    @After
    public void tearDown() {
        greenMail.stop();
    }
    @Test
    public void sendMail() throws IOException, MessagingException {
        GreenMailUser greenMailUser = greenMail.setUser(USER, PASSWORD);
        File f = temporaryFolder.newFile("attached.xml");
        ServerSetup serverSetup = greenMail.getSmtp().getServerSetup();
        Session session = GreenMailUtil.getSession(serverSetup);
        MimeMessage mimeMessage = MimeMessageBuilder.buildMimeMessage(f, "test@mail.com", "user@greenmail.com", "messageText", "subject", session);
        greenMailUser.deliver(mimeMessage);
        SendXML fetchXML = new SendXML();
        File diskFolder = temporaryFolder.newFolder();
        fetchXML.send(new SendOptions(f, false, USER, PASSWORD, null, false, false, 50), "test@greenmail.com", "from@mail.com", "127.0.0.1", serverSetup.getPort());
        greenMail.waitForIncomingEmail(5000, 2);
        Assert.assertEquals(2, greenMail.getReceivedMessages().length);
    }

    @Test
    @Ignore("Due to greenmail version upgrade")
    public void sendMailTls() throws IOException, MessagingException {
        File f = temporaryFolder.newFile("attached.xml");
        ServerSetup serverSetup = greenMail.getSmtps().getServerSetup();
        GreenMailUtil.getSession(serverSetup);
        SendXML sendXML = new SendXML();
        temporaryFolder.newFolder();
        sendXML.send(new SendOptions(f, false, USER, PASSWORD, null, false, true, 50), "test@greenmail.com", "from@mail.com", "127.0.0.1", serverSetup.getPort());
        greenMail.waitForIncomingEmail(10000, 1);
        Assert.assertEquals(1, greenMail.getReceivedMessages().length);
    }
}
