package pl.com.ids;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MimeMessageBuilder {
    public static MimeMessage buildMimeMessage(File f, String to, String from, String msgText1, String subject, Session session) throws MessagingException, IOException {
        // create a message
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        InternetAddress[] address = { new InternetAddress(to) };
        msg.setRecipients(Message.RecipientType.TO, address);
        msg.setSubject(subject);

        // create and fill the first message part
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText(msgText1);

        // create the second message part
        MimeBodyPart mbp2 = new MimeBodyPart();

        // attach the file to the message
        mbp2.attachFile(f.getCanonicalPath());

        // create the Multipart and add its parts to it
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);
        mp.addBodyPart(mbp2);

        // add the Multipart to the message
        msg.setContent(mp);

        // ustawienie daty wyslania listu
        msg.setSentDate(new Date());
        return msg;
    }

}
