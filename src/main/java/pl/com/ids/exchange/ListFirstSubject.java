package pl.com.ids.exchange;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ConnectingIdType;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.ImpersonatedUserId;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

import javax.mail.Message;
import java.net.URI;

public class ListFirstSubject {
    private static final String PATH_EWS = "EWS/Exchange.asmx";

    public static void main(String[] args) throws Exception {
        ListFirstSubject lfs = new ListFirstSubject();
        Options parse = lfs.parse(args);
        lfs.run(parse);
    }

    private void run(Options parse) throws Exception {

        ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        ExchangeCredentials credentials = new BearerTokenCredentials(getBearerToken());
      //  service.setCredentials(credentials);


        if (parse.hostUrl == null) {
            AuthTokenAccess authTokenAccess = new AuthTokenAccess();
            service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
            service.getHttpHeaders().put("Authorization", "Bearer " + getBearerToken() );
            service.getHttpHeaders().put("X-AnchorMailbox", parse.userName);
            service.setImpersonatedUserId(new ImpersonatedUserId(ConnectingIdType.PrincipalName, WellKnownFolderName.Inbox.name()));
        } else {
            ExchangeCredentials webCredentials = new WebCredentials("emcs", "Startowe123!");
            service.setCredentials(webCredentials);
            service.setUrl(new URI("https://" + parse.hostUrl + "/" + PATH_EWS));
        }

        service.setTraceEnabled(true);
        listFirstItemSubject(service);
    }

    private String getBearerToken() {
        return "eyJ0eXAiOiJKV1QiLCJub25jZSI6InBFaWRxQnlPeVRUTUNXWVkwVnFLNTFueTRORmZTT2JDTUg5WXNGVEV2ZTgiLCJhbGciOiJSUz" +
                "I1NiIsIng1dCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyIsImtpZCI6ImpTMVhvMU9XRGpfNTJ2YndHTmd2UU8yVnpNYyJ9" +
                ".eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC85ZTE2NzgwZ" +
                "S03MTlkLTQ5NmItYTg4MC0yNGFjMjBjMzBhNjAvIiwiaWF0IjoxNjUxMDEyMTQ4LCJuYmYiOjE2NTEwMTIxNDgsImV4cCI6MTY1MTA" +
                "xNjk4NywiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkUyWmdZSmkyVzBYWVk5T0JmSXQzVTROVFQ1dk1qbHdWS2JjbW1ORnNub1RQe" +
                "jVpSTkxTUEiLCJhbXIiOlsicHdkIl0sImFwcF9kaXNwbGF5bmFtZSI6Ik9mZmljZTM2NSBTaGVsbCBXQ1NTLUNsaWVudCIsImFwcGl" +
                "kIjoiODliZWUxZjctNWU2ZS00ZDhhLTlmM2QtZWNkNjAxMjU5ZGE3IiwiYXBwaWRhY3IiOiIwIiwiZmFtaWx5X25hbWUiOiJXYXJzY" +
                "XciLCJnaXZlbl9uYW1lIjoiUHJpbnRlciIsImlkdHlwIjoidXNlciIsImlwYWRkciI6Ijk1LjQ5LjIzNC4zOSIsIm5hbWUiOiJQcml" +
                "udGVyIFdhcnNhdyIsIm9pZCI6ImY1ODUwOTAzLTkyMDUtNGM5My1iYzRkLWRjNGM5M2Y4YjZiZSIsIm9ucHJlbV9zaWQiOiJTLTEtN" +
                "S0yMS03ODkzMzYwNTgtNDQ4NTM5NzIzLTgzOTUyMjExNS0yODM1ODEiLCJwbGF0ZiI6IjMiLCJwdWlkIjoiMTAwMzIwMDFEREQwREJ" +
                "GRSIsInJoIjoiMC5BUTBBRG5nV25wMXhhMG1vZ0NTc0lNTUtZQU1BQUFBQUFBQUF3QUFBQUFBQUFBQU5BRE0uIiwic2NwIjoiZW1ha" +
                "WwgRmlsZXMuUmVhZFdyaXRlIE1haWxib3hTZXR0aW5ncy5SZWFkIE1haWxib3hTZXR0aW5ncy5SZWFkV3JpdGUgb3BlbmlkIHByb2Z" +
                "pbGUgVXNlci5SZWFkV3JpdGUiLCJzaWduaW5fc3RhdGUiOlsia21zaSJdLCJzdWIiOiJuTTZLY1IwSDhka2pfWHZqRlk1Y3M5U2lVc" +
                "HQtTTFzdzc3Q2hMSzIzQTlrIiwidGVuYW50X3JlZ2lvbl9zY29wZSI6Ik5BIiwidGlkIjoiOWUxNjc4MGUtNzE5ZC00OTZiLWE4ODA" +
                "tMjRhYzIwYzMwYTYwIiwidW5pcXVlX25hbWUiOiJwcmludGVyd2Fyc2F3QGJhY2FyZGkuY29tIiwidXBuIjoicHJpbnRlcndhcnNhd" +
                "0BiYWNhcmRpLmNvbSIsInV0aSI6IkxvY3hoN2FELWtPcm42V3JFS3owQUEiLCJ2ZXIiOiIxLjAiLCJ3aWRzIjpbImI3OWZiZjRkLTN" +
                "lZjktNDY4OS04MTQzLTc2YjE5NGU4NTUwOSJdLCJ4bXNfc3QiOnsic3ViIjoieGJaTjd2ejFlZnlBeFBoWTRnNjV0WUNSUDJ5Ykloa" +
                "U45RThDdF9sWDd2ZyJ9LCJ4bXNfdGNkdCI6MTM5OTQ2OTM2OH0.sNepRV77341MN8OjvlUcm4qWg5_4TpDksoo2LMmFL5BEIRcY6CL" +
                "gincN5893HCfHAGS9iJ0HI4cVNJu_9tdvSjUkEXuPEnArrH2TEtY8CD_s8P_nTanMX0aQGINHlkiuPI5WoCGHwCGsvOrQw8Fsrl6kV" +
                "z22YdsH7V8tBir-LSBn-nTWj2_7pFVPQt-exvQ0y9mhSiguYhV1gCF0csNys32IFBfWMoIGE1cSW042JgErpWBIxP5kDgjfdRCKmFm" +
                "p51Gm1ijUESeSaNbXSmYH5icedoeC2gUHRwqhVb-D4KftnpWYVnUdoLPOHf2IOdtMciv6xYcLDQSMrqdRy0VBiQ";
    }

    private void listFirstItemSubject(ExchangeService service) throws Exception {
        ItemView view = new ItemView(5);
        WellKnownFolderName inbox = WellKnownFolderName.Inbox;
        FindItemsResults<Item> findResults = service.findItems(inbox, view);

        //MOOOOOOST IMPORTANT: load messages' properties before
        service.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);

        for (Item item : findResults.getItems()) {
            // Do something with the item as shownFetch
            //System.out.println("id==========" + item.getId());
            System.out.println("sub==========" + item.getSubject());
            if (item instanceof EmailMessage) {
                EmailMessage emailMessage = (EmailMessage) item;
                System.out.println("from: " +  emailMessage.getSender().getName() + "<" + emailMessage.getSender().getAddress() + ">");
            }
            Thread.sleep(2000);
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
