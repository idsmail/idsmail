package pl.com.ids;


import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.Security;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import static pl.com.ids.MimeMessageBuilder.buildMimeMessage;

class Logger {
    private BufferedWriter bwr;
    private String filename;
    private String logFolder = "";

    public Logger(final String filename) {
        this.filename = filename;
    }

    private String addSlash(String folder) {
        char lastChar = folder.charAt(folder.length() - 1);
        if (lastChar != '\\') {
            folder += "\\";
        }
        return folder;


    }

    public void setLogFolder(File logFolder) {
        if (logFolder != null) {
            this.logFolder = addSlash(logFolder.getAbsolutePath());
        } else {
            this.logFolder = null;
        }

    }

    public void logExit(int result) {
        try {
            bwr = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(logFolder + filename)));
        } catch (FileNotFoundException fnf) {
            System.out.println(fnf);
        }

        try {
            bwr.write("" + result);
            bwr.close();
            System.exit(result);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }

    }

    public void logExit(int result, String details) {
        BufferedWriter bwrExtended = null;
        try {
            bwrExtended = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("outDetails.txt")));
        } catch (FileNotFoundException fnf) {
            System.out.println(fnf);
            System.exit(result);
        }
        try {
            if (bwr != null) {
                bwr.write("" + result);
                bwr.close();
            }
            bwrExtended.write(details);
            bwrExtended.close();
            System.exit(result);

            System.exit(result);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }

    }
}

public class SendXML {

    /**
     * @param args
     */

    private static boolean debug = false;
    /*
     * Zwracane parametry
     */
    private static final int ERR_SYNTAX_ERROR = 2; // blad skladni
    static final int ERR_FILE_NOT_FOUND = 3; // blad nie znaleziono pliku
    private static final int ERR_FILE_NOT_SENT = 4; // nie wyslano pliku

    private static void printUsage(OptionParser parser) throws IOException {
        System.err
                .println("Usage: sendXML [-s] [-c] -h smtp_server_name -m port [-a -u user -p pass] \n"
                        + "                   file_name address_to address_from -v -l log_folder");
        parser.printHelpOn(System.out);
    }

    public static void main(String[] args) throws IOException {
        Logger l = new Logger("out.txt");
        if (debug) {
            for (int i = 0; i < args.length; i++) {
                System.out.println(args[i]);
            }
        }

        OptionParser parser = new OptionParser();
        OptionSpec<String> opHost = parser.accepts("h", "host to connect").withRequiredArg().ofType(String.class);
        OptionSpec<Void> opSsl = parser.accepts("s", "ssl usage");
        OptionSpec<Void> opTls = parser.accepts("c", "tls usage");
        OptionSpec<Integer> opPort = parser.accepts("m", "port number").withRequiredArg().ofType(Integer.class);
        OptionSpec<Integer> opTimeout = parser.accepts("t", "timeout").withRequiredArg().ofType(Integer.class);

        OptionSpec<Void> opAuthenticate = parser.accepts("a", "authenticate");
        OptionSpec<String> opUser = parser.accepts("u", "username").withRequiredArg().ofType(String.class);
        OptionSpec<String> opPassword = parser.accepts("p", "pass").withRequiredArg().ofType(String.class);
        OptionSpec<File> opLogFolder = parser.accepts("l", "logFolder").withRequiredArg().ofType(File.class);
        OptionSpec<Void> opVerbose = parser.accepts("v", "verbose");

        OptionSet options = parser.parse(args);

        final File logFolder = options.valueOf(opLogFolder);
        l.setLogFolder(logFolder);

        debug = options.has(opVerbose);
        Boolean auth = options.has(opAuthenticate);
        String user = null;
        String password = null;
        String host = null;
        Integer port = null;
        if (auth != null && auth) {
            user = options.valueOf(opUser);
            password = options.valueOf(opPassword);
        }
        host = options.valueOf(opHost);
        port = options.valueOf(opPort);

        List<?> otherArgs = options.nonOptionArguments();
        if (debug)
            System.out.println("remaining args: (  " + otherArgs.size() + ")");

        if (debug) {
            otherArgs.stream().forEach(System.out::println);
        }

        Boolean ssl = options.has(opSsl);
        Boolean tls = options.has(opTls);

        if (otherArgs.size() != 3 || host == null || port == null) {
            printUsage(parser);
            l.logExit(ERR_SYNTAX_ERROR);
        }
        if (auth != null && auth.booleanValue()
                && (user == null || password == null)) {
            printUsage(parser);
            l.logExit(ERR_SYNTAX_ERROR);
        }

        File f = new File((String) otherArgs.get(0));
        if (!f.exists()) {
            l.logExit(ERR_FILE_NOT_FOUND);
        }
        if (debug) {
            System.out.println("User: " + user);
            System.out.println("Password: " + password);
            System.out.println("Host: " + host);
        }
        SendXML sx = new SendXML();

        Integer timeout = options.valueOf(opTimeout);
        sx.send(f, (String) otherArgs.get(1), (String) otherArgs.get(2), host, port, auth, user, password, l, ssl, tls, timeout);
        l.logExit(0);
    }

    public void send(File f, String to, String from, String smtphost, Integer smtpport,
                     Boolean auth, String username, String password, Logger l, Boolean ssl, Boolean tls, Integer timeout) {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        /**
         * sendfile will create a multipart message with the second block of the
         * message being the given file.
         */


        String msgText1 = "Sending a file " + f.getName() + " .\n";
        String subject = "Sending a file " + f.getName();

        // create some properties and get the default Session
        Properties props = System.getProperties();
        props.put("mail.smtp.host", smtphost);
        props.put("mail.smtp.port", smtpport);

        props.put("mail.smtp.port", smtpport);
        if (timeout != null) {
            props.put("mail.smtp.connectiontimeout", timeout * 1000);
            props.put("mail.smtp.timeout", timeout * 1000);
            props.put("mail.smtp.writetimeout", timeout * 1000);
        }

        if (ssl != null && ssl) {
            props.put("mail.smtp.ssl.enable", ssl.booleanValue());
            props.put("mail.smtp.socketFactory.port", smtpport);
            props.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        }

        if (tls != null && tls) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        if (auth != null && auth.booleanValue()) {
            props.put("mail.smtp.auth", "true");
            props.setProperty("mail.user", username);
            props.setProperty("mail.password", password);

        }
        Session session = Session.getInstance(props, null);

        session.setDebug(debug);

        try {
            MimeMessage msg = buildMimeMessage(f, to, from, msgText1, subject, session);

            Transport tr = session.getTransport("smtp");
            tr.connect(smtphost, username, password);
            msg.saveChanges(); // bardzo istotna linia
            tr.sendMessage(msg, msg.getAllRecipients());
            tr.close();

        } catch (MessagingException mex) {

            // mex.printStackTrace();
            // Exception ex = null;
            // if ((ex = mex.getNextException()) != null) {
            // ex.printStackTrace();
            // }
            if (l != null) {
                l.logExit(ERR_FILE_NOT_SENT);
            }

        } catch (IOException ioex) {
            ioex.printStackTrace();
            if (l != null) {
                l.logExit(ERR_FILE_NOT_SENT);
            }
        }
    }

}
