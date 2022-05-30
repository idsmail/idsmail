package pl.com.ids.application.outlook365;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.AttachmentCollectionResponse;
import com.microsoft.graph.requests.GraphServiceClient;
import org.jetbrains.annotations.NotNull;
import pl.com.ids.infrastructure.IdsLogger;
import pl.com.ids.infrastructure.SimpleDebugger;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.Properties;

public class SendEmailUsingGraphApi {
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
        String accessToken = null;
        String refreshToken = null;
        try {
            JsonParser parser = executeCall(endpoint, postBody);
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

    private JsonParser executeCall(String endpoint, String postBody) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        conn.getOutputStream().write(postBody.getBytes());
        conn.connect();
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(conn.getInputStream());
        return parser;
    }

    private void sendEmail(GraphServiceClient graphClient, Properties props) throws IOException {
        String fileName = props.getProperty(SendOptionsProcessor.FILE_NAME);
        String msgText1 = "Sending a file " + fileName + " .\n";
        String subject = "Sending a file " + fileName;

        Message message = new Message();
        message.subject = subject;
        ItemBody body = new ItemBody();
        body.contentType = BodyType.TEXT;
        body.content = msgText1;
        message.body = body;
        LinkedList<Attachment> attachmentsList = new LinkedList<>();
        byte[] content = Files.readAllBytes(new File(fileName).toPath());
        FileAttachment attachment = new FileAttachment();
        attachment.name = fileName;
        attachment.contentType = "text/plain";
        attachment.oDataType = "#microsoft.graph.fileAttachment";
        attachment.contentBytes = content;
        attachmentsList.add(attachment);
        AttachmentCollectionResponse attachmentCollectionResponse = new AttachmentCollectionResponse();
        attachmentCollectionResponse.value = attachmentsList;
        AttachmentCollectionPage attachmentCollectionPage = new AttachmentCollectionPage(attachmentCollectionResponse, null);
        message.attachments = attachmentCollectionPage;
        message.toRecipients = buildRecipients(props.getProperty(SendOptionsProcessor.ADDRESS_TO));
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

    @NotNull
    private LinkedList<Recipient> buildRecipients(String toAddress) {
        LinkedList<Recipient> toRecipientsList = new LinkedList<Recipient>();
        Recipient toRecipients = new Recipient();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.address = toAddress;
        toRecipients.emailAddress = emailAddress;
        toRecipientsList.add(toRecipients);
        return toRecipientsList;
    }

    public static void main(String[] args) throws IOException {
        // client id od aplikacji https://portal.azure.com/#blade/Microsoft_AAD_RegisteredApps/ApplicationMenuBlade/Overview/appId/4e33db79-7e64-4bc7-a482-ad24de40913d/isMSAApp/true
        // dostep na moj gmail
        SendOptionsProcessor sendOptionsProcessor = new SendOptionsProcessor();
        Properties props = sendOptionsProcessor.getSendOptions(args, new IdsLogger("sendMail.log"), new SimpleDebugger());
        String clientId = props.getProperty("clientId");
        String clientSecret = props.getProperty("clientSecret");

        SendEmailUsingGraphApi sendEmailUsingGraphApi = new SendEmailUsingGraphApi();
        if (props.getProperty("refreshToken") == null) {
            System.out.println("brak refresh tokenu, uruchom fetch, aby go wygenerować");
            System.exit(2);
        }
        String refreshToken = props.getProperty("refreshToken");
        String accessToken = sendEmailUsingGraphApi.refreshToken(refreshToken, clientId, clientSecret);
        GraphServiceClient client = GraphServiceClient.builder().authenticationProvider(new TokenCredentialAuthProvider(new TokenCredential() {
            @Override
            public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
                return Mono.just(new AccessToken(accessToken, OffsetDateTime.MAX));
            }
        })).buildClient();


        sendEmailUsingGraphApi.sendEmail(client, props);
    }
}
