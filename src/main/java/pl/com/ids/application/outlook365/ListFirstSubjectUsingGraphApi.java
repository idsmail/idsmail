package pl.com.ids.application.outlook365;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.GraphServiceClient;
import pl.com.ids.exchange.AuthTokenAccess;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;

public class ListFirstSubjectUsingGraphApi {
    private String decode(String value) throws UnsupportedEncodingException {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
    }

    public String getAuthorizationCode(String tenantId, String clientId) throws IOException {
        String endpoint = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?client_id=%s" +
                "&response_type=code" +
                "&redirect_uri=%s" +
                "&response_mode=query" +
                "&scope=%s" +
                "&state=12345", tenantId, clientId, decode("http://localhost:3000/auth/callback"), "offline_access%20user.read%20mail.read%20mail.send");
        System.out.println(endpoint);
        //
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        return sb.toString();
    }

    public String getRefreshToken(String tenantId, String clientId, String clientSecret, String code) throws UnsupportedEncodingException {
        String endpoint = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token ", tenantId);
        String postBody = String.format("grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s",
                code, decode("http://localhost:3000/auth/callback"), clientId, clientSecret);
        System.out.println(String.format("$body = @{\n\"grant_type\"=\"authorization_code\"\n\"code\"=\"%s\"\n\"redirect_uri\"=\"%s\"\n\"client_id\"=\"%s\"\n\"client_secret\"=\"%s\"\n}", code, decode("http://localhost:3000/auth/callback"), clientId, clientSecret));
        String accessToken = null;
        String refreshToken = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.getOutputStream().write(postBody.getBytes());
            conn.connect();
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(conn.getInputStream());
            //String accessToken = null;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String name = parser.getCurrentName();
                if ("access_token".equals(name)) {
                    parser.nextToken();
                    accessToken = parser.getText();
                }
                if ("refresh_token".equals(name)) {
                    parser.nextToken();
                    refreshToken = parser.getText();
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return refreshToken;
    }

    private void sendEmail(GraphServiceClient graphClient) {
        Message message = new Message();
        message.subject = "Meet for lunch?";
        ItemBody body = new ItemBody();
        body.contentType = BodyType.TEXT;
        body.content = "The new cafeteria is open.";
        message.body = body;
        LinkedList<Recipient> toRecipientsList = new LinkedList<Recipient>();
        Recipient toRecipients = new Recipient();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = "karol.kalinski@gmail.com";
        toRecipients.emailAddress = emailAddress;
        toRecipientsList.add(toRecipients);
        message.toRecipients = toRecipientsList;
        boolean saveToSentItems = false;

        graphClient.me()
                .sendMail(UserSendMailParameterSet
                        .newBuilder()
                        .withMessage(message)
                        .withSaveToSentItems(saveToSentItems)
                        .build())
                .buildRequest()
                .post();
    }

    public static void main(String[] args) throws IOException {
        // client id od aplikacji https://portal.azure.com/#blade/Microsoft_AAD_RegisteredApps/ApplicationMenuBlade/Overview/appId/4e33db79-7e64-4bc7-a482-ad24de40913d/isMSAApp/true
        // dostep na moj gmail
        String cfgFileName = "app.cfg";
        String clientId = "4e33db79-7e64-4bc7-a482-ad24de40913d";
        String clientSecret = "W148Q~_ETj2BMjzWQcrxBcaHD67kaDu0rA8TIdm4";

        Properties props = new Properties();
        props.load(new FileReader(cfgFileName));
        String accessTokenClientCredentials = AuthTokenAccess.getAccessToken("common", clientId, clientSecret, "mail.read");

        ListFirstSubjectUsingGraphApi listFirstSubjectUsingGraphApi = new ListFirstSubjectUsingGraphApi();
        if (!new java.io.File(cfgFileName).exists()) {
            String common = listFirstSubjectUsingGraphApi.getAuthorizationCode("common", clientId);
            Scanner scanner = new Scanner(System.in);
            String code = scanner.nextLine();
            System.out.println("Your string: " + code);
            String refreshToken = listFirstSubjectUsingGraphApi.getRefreshToken("common", clientId, clientSecret, code);
            Properties prop = new Properties();
            prop.setProperty("refresh_token", refreshToken);
            prop.store(new FileOutputStream(cfgFileName), null);
        }

        System.out.println(Base64.getDecoder().decode(accessTokenClientCredentials.split("\\.")[1]));
        GraphServiceClient client = GraphServiceClient.builder().authenticationProvider(new TokenCredentialAuthProvider(new TokenCredential() {
            @Override
            public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
                return Mono.just(new AccessToken(accessTokenClientCredentials, OffsetDateTime.MAX));
            }
        })).buildClient();

        //https://docs.microsoft.com/en-us/graph/api/resources/message?view=graph-rest-1.0
        List<Message> messages = client.me().mailFolders("Inbox").messages().buildRequest().select("sender,subject").get().getCurrentPage();
        messages.forEach(m -> {
            System.out.println(m.subject);
            System.out.println(m.attachments.getCount());
        });

        listFirstSubjectUsingGraphApi.sendEmail(client);
    }
}
