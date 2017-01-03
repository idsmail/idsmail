package pl.com.ids.exchange;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

import java.net.URI;

public class ListFirstSubject {
    public static void main(String[] args) throws Exception {
        ListFirstSubject lfs = new ListFirstSubject();
        Options parse = lfs.parse(args);
        lfs.run(parse);
    }

    private void run(Options parse) throws Exception {
        ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        ExchangeCredentials credentials = new WebCredentials(parse.userName, parse.password);
        service.setCredentials(credentials);
        if (parse.hostUrl == null) {
            service.autodiscoverUrl(parse.userName);
        } else {
            service.setUrl(parse.hostUrl);
        }
        listFirstItemSubject(service);
    }

    private void listFirstItemSubject(ExchangeService service) throws Exception {
        ItemView view = new ItemView(1);
        WellKnownFolderName inbox = WellKnownFolderName.Inbox;
        FindItemsResults<Item> findResults = service.findItems(inbox, view);

        //MOOOOOOST IMPORTANT: load messages' properties before
        service.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);

        for (Item item : findResults.getItems()) {
            // Do something with the item as shownFetch
            System.out.println("id==========" + item.getId());
            System.out.println("sub==========" + item.getSubject());
        }
    }

    private Options parse(String args[]) {
        OptionParser parser = new OptionParser();
        OptionSpec<String> userNameOp = parser.accepts("u", "emailAddress").withRequiredArg();
        OptionSpec<String> passwordOp = parser.accepts("p", "password").withRequiredArg();
        OptionSpec<URI> hostUrlOp = parser.accepts("h", "hostUrl").withRequiredArg().ofType(URI.class);
        OptionSet parse = parser.parse(args);
        String userName = parse.valueOf(userNameOp);
        String password = parse.valueOf(passwordOp);
        URI hostUrl = parse.valueOf(hostUrlOp);
        return new Options(userName, password, hostUrl);
    }

    private class Options {
        public final String userName;
        public final String password;
        public final URI hostUrl;

        public Options(String userName, String password, URI hostUrl) {
            this.userName = userName;
            this.password = password;
            this.hostUrl = hostUrl;
        }
    }
}
