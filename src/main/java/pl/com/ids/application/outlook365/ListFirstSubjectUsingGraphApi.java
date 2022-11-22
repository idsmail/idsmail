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
import reactor.core.publisher.Mono;

import java.io.*;
import java.io.File;
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

    public String refreshToken(String refreshToken, String clientId, String clientSecret) {
        String tenantId = "common";
        String endpoint = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token ", tenantId);
        String accessToken = null;
        try {
            String postBody = String.format("grant_type=refresh_token&refresh_token=%s&redirect_uri=%s&client_id=%s&client_secret=%s&scope=%s",
                    refreshToken, decode("http://localhost:3000/auth/callback"), clientId, clientSecret, "offline_access%20user.read%20mail.read%20mail.send");
            System.out.println(String.format("$body = @{\n\"grant_type\"=\"refresh_token\"\n\"refresh_token\"=\"%s\"\n\"redirect_uri\"=\"%s\"\n\"client_id\"=\"%s\"\n\"client_secret\"=\"%s\"\n\"scope\"=\"%s\"}", refreshToken, decode("http://localhost:3000/auth/callback"), clientId, clientSecret, "offline_access%20user.read%20mail.read%20mail.send"));
            JsonParser parser = executeCall(endpoint, postBody);
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String name = parser.getCurrentName();
                if ("access_token".equals(name)) {
                    parser.nextToken();
                    accessToken = parser.getText();
                }
            }
            return accessToken;

        } catch (IOException e) {
            e.printStackTrace();
        }
        //    Content-Type: application/x-www-form-urlencoded
//                &scope=https%3A%2F%2Fgraph.microsoft.com%2Fmail.read
        return null;
    }

    public String getRefreshToken(String tenantId, String clientId, String clientSecret, String code) throws UnsupportedEncodingException {
        String endpoint = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token ", tenantId);
        String postBody = String.format("grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s",
                code, decode("http://localhost:3000/auth/callback"), clientId, clientSecret);
        System.out.println(String.format("$body = @{\n\"grant_type\"=\"authorization_code\"\n\"code\"=\"%s\"\n\"redirect_uri\"=\"%s\"\n\"client_id\"=\"%s\"\n\"client_secret\"=\"%s\"\n}", code, decode("http://localhost:3000/auth/callback"), clientId, clientSecret));
        String refreshToken = null;
        try {
            JsonParser parser = executeCall(endpoint, postBody);
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String name = parser.getCurrentName();
                if ("access_token".equals(name)) {
                    parser.nextToken();
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

    private JsonParser executeCall(String endpoint, String postBody) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        conn.getOutputStream().write(postBody.getBytes());
        JsonFactory factory = new JsonFactory();
        try {
            conn.connect();
            JsonParser parser = factory.createParser(conn.getInputStream());
            return parser;
        } catch (IOException ioe) {
            JsonParser parser = factory.createParser(conn.getErrorStream());
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String name = parser.getCurrentName();
                if ("error_description".equals(name)) {
                    System.out.print("Error occurred: ");
                    System.out.println(parser.getText());
                }
                parser.nextToken();
            }
            throw ioe;
        }
    }

    public static void main(String[] args) throws IOException {
        // client id od aplikacji https://portal.azure.com/#blade/Microsoft_AAD_RegisteredApps/ApplicationMenuBlade/Overview/appId/4e33db79-7e64-4bc7-a482-ad24de40913d/isMSAApp/true
        // dostep na moj gmail
        String cfgFileName = args.length > 0 && args[0] != null ? args[0] : "app.cfg";
        if (!new File(cfgFileName).exists()) {
            System.out.println(String.format("plik cfg nie istnieje %s", cfgFileName));
            System.exit(3);
        }
        Properties props = new Properties();
        props.load(new FileReader(cfgFileName));

        String clientId = props.getProperty("clientId");
        String clientSecret = props.getProperty("clientSecret");

        ListFirstSubjectUsingGraphApi listFirstSubjectUsingGraphApi = new ListFirstSubjectUsingGraphApi();
        if (props.getProperty("refreshToken") == null) {
            String common = listFirstSubjectUsingGraphApi.getAuthorizationCode("common", clientId);
            Scanner scanner = new Scanner(System.in);
            String code = scanner.nextLine();
            System.out.println("Your string: " + code);
            String refreshToken = listFirstSubjectUsingGraphApi.getRefreshToken("common", clientId, clientSecret, code);
            props.setProperty("refreshToken", refreshToken);
            props.store(new FileOutputStream(cfgFileName), null);
            System.exit(15);
        }
        String refreshToken = props.getProperty("refreshToken");
        String accessToken = listFirstSubjectUsingGraphApi.refreshToken(refreshToken, clientId, clientSecret);
        GraphServiceClient client = GraphServiceClient.builder().authenticationProvider(new TokenCredentialAuthProvider(new TokenCredential() {
            @Override
            public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
                return Mono.just(new AccessToken(accessToken, OffsetDateTime.MAX));
            }
        })).buildClient();

        //https://docs.microsoft.com/en-us/graph/api/resources/message?view=graph-rest-1.0
        List<Message> messages = client.me().mailFolders("Inbox").messages().buildRequest().select("sender,subject").get().getCurrentPage();
        messages.forEach(m -> {
            System.out.println(m.subject);
            List<Attachment> attachments = client.me().messages(m.id).attachments().buildRequest().get().getCurrentPage();
            attachments.forEach(a -> {
                FileAttachment attachment = (FileAttachment) client.me().messages(m.id).attachments(a.id)
                        .buildRequest()
                        .get();
                File outputFile = new File(attachment.name);

                try {
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    outputStream.write(attachment.contentBytes);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.exit(4);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(4);
                }

            });

        });

    }
}
