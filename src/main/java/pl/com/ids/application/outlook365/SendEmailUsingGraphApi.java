package pl.com.ids.application.outlook365;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.Properties;

public class SendEmailUsingGraphApi {
    private void sendEmail(GraphServiceClient graphClient, Properties props) throws IOException {
        String fullFilePath = props.getProperty(SendOptionsProcessor.FILE_NAME);

        // create object of Path
        Path path = Paths.get(fullFilePath);

        // call getFileName() and get FileName path object
        Path fileName = path.getFileName();

        String msgText1 = "Sending a file " + fileName + " .\n";
        String subject = "Sending a file " + fileName;

        Message message = new Message();
        message.subject = subject;
        ItemBody body = new ItemBody();
        body.contentType = BodyType.TEXT;
        body.content = msgText1;
        message.body = body;
        LinkedList<Attachment> attachmentsList = new LinkedList<>();
        byte[] content = Files.readAllBytes(new File(fullFilePath).toPath());
        FileAttachment attachment = new FileAttachment();
        attachment.name = fileName.toString();
        attachment.contentType = "text/plain";
        attachment.oDataType = "#microsoft.graph.fileAttachment";
        attachment.contentBytes = content;
        attachmentsList.add(attachment);
        AttachmentCollectionResponse attachmentCollectionResponse = new AttachmentCollectionResponse();
        attachmentCollectionResponse.value = attachmentsList;
        message.attachments = new AttachmentCollectionPage(attachmentCollectionResponse, null);
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
        LinkedList<Recipient> toRecipientsList = new LinkedList<>();
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
        IdentityPlatfomClient identityPlatfomClient = new IdentityPlatfomClient();

        SendEmailUsingGraphApi sendEmailUsingGraphApi = new SendEmailUsingGraphApi();
        if (props.getProperty("refreshToken") == null) {
            System.out.println("brak refresh tokenu, uruchom fetch, aby go wygenerować");
            System.exit(5);
        }
        String refreshToken = props.getProperty("refreshToken");
        TokenPair tokenPair = identityPlatfomClient.refreshToken(refreshToken, clientId, clientSecret);
        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(tokenRequestContext -> Mono.just(new AccessToken(tokenPair.getAccessToken(), OffsetDateTime.MAX)));
        GraphServiceClient client = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();


        sendEmailUsingGraphApi.sendEmail(client, props);
    }
}
