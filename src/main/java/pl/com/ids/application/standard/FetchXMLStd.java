package pl.com.ids.application.standard;


import pl.com.ids.*;
import pl.com.ids.domain.Configuration;
import pl.com.ids.domain.FetchOptions;
import pl.com.ids.domain.FetchXML;
import pl.com.ids.infrastructure.Debugger;
import pl.com.ids.infrastructure.DebuggerFactory;
import pl.com.ids.infrastructure.IdsLogger;

import javax.mail.*;
import java.io.IOException;
import java.util.Properties;

public class FetchXMLStd {
    private Debugger debugger;

    public FetchXMLStd(Debugger debugger) {
        this.debugger = debugger;
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        IdsLogger l = new IdsLogger("in.txt");
        OptionsFetcher optionsFetcher = new OptionsFetcher(args);
        Debugger debugger = DebuggerFactory.getDebugger(args);

        Configuration<Service> configuration = optionsFetcher.getFetchOptions(l, debugger);

        FetchXMLStd fx = new FetchXMLStd(debugger);

        fx.fetch(configuration);
        l.logSuccess();
        // TODO Auto-generated method stub

    }

    public void fetch(Configuration<Service> configuration) {
        FetchOptions fetchOptions = configuration.getFetchOptions();
        StatusNotifier statusNotifier = new StatusNotifier(fetchOptions.getDiskFolder());
        statusNotifier.updateStatus(ProcessingStatus.WORKING);

        Properties props = new Properties();
        fetchOptions.getTimeout().ifPresent(to -> {
            props.setProperty("mail.pop3.connectiontimeout", to.toString());
            props.setProperty("mail.pop3.timeout", to.toString());
        });

        Service service = configuration.getSpecific();
        if (service.getUseTls()) {
            props.setProperty("mail.pop3.starttls.enable", "true");
        }

        if (service.getProtocol() == Protocol.POP3S) {
            props.put("mail.pop3s.ssl.trust", "*");

            props.setProperty("mail.pop3.socketFactory.fallback", "false");
        }
        Folder folder = null;

        Store store = null;
        try {
            store = getConnectedStore(service, props, fetchOptions.isDebug());

            folder = store.getFolder("INBOX");
            new FetchXML(debugger).processFolder(fetchOptions, folder);
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

}
