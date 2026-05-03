package pl.com.ids.application.outlook365;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
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
        message.setSubject(subject);

        ItemBody body = new ItemBody();
        body.setContentType(BodyType.Text); // Uwaga: wielkość liter w typie wyliczeniowym
        body.setContent(msgText1);
        message.setBody(body);

        LinkedList<Attachment> attachmentsList = new LinkedList<>();
        byte[] content = Files.readAllBytes(new File(fullFilePath).toPath());

        FileAttachment attachment = new FileAttachment();
        attachment.setName(fileName.toString());
        attachment.setContentType("text/plain");
        attachment.setOdataType("#microsoft.graph.fileAttachment");
        attachment.setContentBytes(content);

        attachmentsList.add(attachment);
        message.setAttachments(attachmentsList); // Pamiętaj o przypisaniu listy do obiektu wiadomości

        // 1. W v6 po prostu przypisujesz listę załączników do obiektu wiadomości
        message.setAttachments(attachmentsList);

        // 2. Przypisanie odbiorców (pola są teraz prywatne, więc używamy setterów)
        message.setToRecipients(buildRecipients(props.getProperty(SendOptionsProcessor.ADDRESS_TO)));

        boolean saveToSentItems = false;

        // 3. W v6 tworzymy obiekt konfiguracji żądania wysyłki
        com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody requestBody = new com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody();
        requestBody.setMessage(message);
        requestBody.setSaveToSentItems(saveToSentItems);

        // 4. Bezpośrednie wywołanie metody akcji sendMail()
        graphClient.me()
                .sendMail()
                .post(requestBody);
    }

    @NotNull
    private LinkedList<Recipient> buildRecipients(String toAddress) {
        LinkedList<Recipient> toRecipientsList = new LinkedList<>();
        Recipient toRecipients = new Recipient();
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.setAddress(toAddress);
        toRecipients.setEmailAddress(emailAddress);
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

        // 1. Zamiast AuthProvidera tworzymy obiekt TokenCredential za pomocą lambdy
        com.azure.core.credential.TokenCredential credential = tokenRequestContext ->
                Mono.just(new com.azure.core.credential.AccessToken(tokenPair.getAccessToken(), java.time.OffsetDateTime.MAX));

        // 2. Definiujemy domyślny zakres uprawnień (scope) dla Graph API
        String[] scopes = new String[] { "https://microsoft.com" };

        // 3. Tworzymy klienta operatorem 'new', przekazując poświadczenia i zakresy
        GraphServiceClient client = new GraphServiceClient(credential, scopes);


        sendEmailUsingGraphApi.sendEmail(client, props);
    }
}
