package pl.com.ids;


import pl.com.ids.io.AugmentedFileWriter;

import javax.mail.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class FetchXML {
    private Debugger debugger;

    public FetchXML(Debugger debugger) {
        this.debugger = debugger;
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        Logger l = new Logger("in.txt");
        OptionsFetcher optionsFetcher = new OptionsFetcher(args);
        Debugger debugger = DebuggerFactory.getDebugger(args);

        FetchOptions fetchOptions = optionsFetcher.getFetchOptions(l, debugger);

        FetchXML fx = new FetchXML(debugger);
        fx.fetch(fetchOptions);
        l.logExit(0);
        // TODO Auto-generated method stub

    }


    public void fetch(FetchOptions fetchOptions) {
        StatusNotifier statusNotifier = new StatusNotifier(fetchOptions.getDiskFolder());
        statusNotifier.updateStatus(ProcessingStatus.WORKING);

        AugmentedFileWriter fileWriter = new AugmentedFileWriter(fetchOptions.getOverwrite(),
                fetchOptions.isDebug());

        Properties props = new Properties();
        fetchOptions.getTimeout().ifPresent(to -> {
            props.setProperty("mail.pop3.connectiontimeout", to.toString());
            props.setProperty("mail.pop3.timeout", to.toString());
        });

        if (fetchOptions.getService().getUseTls()) {
            props.setProperty("mail.pop3.starttls.enable", "true");
        }

        if (fetchOptions.getService().getProtocol() == Protocol.POP3S) {
            props.put("mail.pop3s.ssl.trust", "*");

            props.setProperty("mail.pop3.socketFactory.fallback", "false");
        }
        Folder folder = null;

        Store store = null;
        try {
            store = getConnectedStore(fetchOptions.getService(), props, fetchOptions.isDebug());

            folder = store.getFolder("INBOX");
            processFolder(fetchOptions, fileWriter, folder);
        } catch (NoSuchProviderException ex) {
            debugger.debug(ex.getMessage());

        } catch (MessagingException | IOException me) {
            debugger.debug(me.getMessage());
            me.printStackTrace();
        } finally {
            statusNotifier.updateStatus(ProcessingStatus.DONE);
            try {
                if (folder != null) {
                    folder.close(true);
                }
                store.close();
            } catch (MessagingException me) {
                me.printStackTrace();
            }
        }
    }

    public void processFolder(FetchOptions fetchOptions, AugmentedFileWriter fileWriter, Folder folder) throws MessagingException, IOException {
        folder.open(Folder.READ_WRITE);
        Message[] messages = folder.getMessages();
        for (int i = 0; i < messages.length; i++) {
            boolean limitReached = processMessage(fetchOptions, fileWriter, messages[i], i);
            if (limitReached) {
                return;
            }
        }
    }

    private boolean processMessage(FetchOptions fetchOptions, AugmentedFileWriter fileWriter, Message message, int i) throws MessagingException, IOException {
        String ct = message.getContentType();

        boolean debug = fetchOptions.isDebug();
        debugger.debug("---- Wiadomosc numer " + i + " -----");

        boolean senderVerified = verifySender(fetchOptions.getSender(), message, debug);
        boolean messageAvailable = !message.isSet(Flags.Flag.DELETED);
        if (senderVerified && messageAvailable && checkDateRange(fetchOptions, message)) {
            debugger.debug("Zawartosc" + ct);
            return saveAttachment(fetchOptions.getDiskFolder(), fetchOptions.getDelete(), fetchOptions.getSingle(),
                    fileWriter, message, ct, fetchOptions.isDebug());
        }
        return false;
    }

    private boolean checkDateRange(FetchOptions fetchOptions, Message message) throws MessagingException {
        DatesComparator dc = new DatesComparator();
        Date sentDate = message.getSentDate();

        boolean result = dc.between(fetchOptions.getDateLowLimit(), fetchOptions.getDateHighLimit(), sentDate);
        if (!result) {
            debugger.debug("Spoza zakresu " + sentDate);
        }
        return result;
    }

    private Store getConnectedStore(Service service, Properties props, boolean debug) throws MessagingException {
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(debug);

        Store store = session.getStore(service.getUrl());
        if (debug) {
            System.out.println("Connect" + service.getHost() + " ; " + service.getUsername());
        }
        store.connect();
        return store;
    }

    private boolean saveAttachment(File diskFolder, Boolean delete,
                                   Boolean single, AugmentedFileWriter fileWriter,
                                   Message message, String ct, boolean debug)
            throws IOException, MessagingException {
        boolean retrived = false;
        if (ct.startsWith("multipart")) {
            Multipart mp = (Multipart) message.getContent();
            // Iterujemy po zawarto�ci listu
            for (int j = 0, n = mp.getCount(); j < n; j++) {
                Part part = mp.getBodyPart(j);

                String disposition = part.getDisposition();
                if (debug)
                    System.out.println("Tutaj :" + part.getContentType() + " "
                            + part.getFileName() + " " + disposition);

                if (Part.ATTACHMENT.equalsIgnoreCase(disposition)) {

                    if (debug)
                        System.out.println("Jestemy w save");

                    fileWriter.storeFile(diskFolder, part.getFileName(),
                            part.getInputStream());
                    if (delete != null && delete) {
                        message.setFlag(Flags.Flag.DELETED, true);
                    }
                    if (single) {
                        retrived = true;
                    }
                } else {
                    if (debug)
                        System.out.println("Jestemy w else");
                }
            }
        }
        return retrived;
    }


    private boolean verifySender(String sender, Message message, boolean debug)
            throws MessagingException {
        final boolean verificationResult;

        if (sender == null) {
            verificationResult = true;
        } else {
            Address[] fromList = message.getFrom();
            String from = "";
            for (Address address : fromList) {
                from += address;
            }

            if (debug) {
                System.out.println("Nadawca " + from);
            }
            verificationResult = from.contains(sender);

        }
        return verificationResult;

    }

}
