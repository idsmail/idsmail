package pl.com.ids.application.outlook365;

import com.azure.core.credential.AccessToken;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

public class ListFirstSubjectUsingGraphApi {
    static void updateRefreshToken(Properties props, String cfgFileName, String refreshToken) throws IOException {
        props.setProperty("refreshToken", refreshToken);

        LocalDateTime refreshTokenExpiryDate = LocalDateTime.now().plusDays(90);
        props.setProperty("refreshTokenExpiryDate", refreshTokenExpiryDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a")));
        props.store(new FileOutputStream(cfgFileName), null);
    }


    public static void main(String[] args) throws IOException {
        // client id od aplikacji https://portal.azure.com/#blade/Microsoft_AAD_RegisteredApps/ApplicationMenuBlade/Overview/appId/4e33db79-7e64-4bc7-a482-ad24de40913d/isMSAApp/true
        // dostep na moj gmail
        if (args.length < 1) {
            System.out.print("nie podano folderu docelowego");
            System.exit(5);
        }

        String destFolder =  args[0];

        String cfgFileName = args.length > 1 && args[1] != null ? args[1] : "app.cfg";
        if (!new File(cfgFileName).exists()) {
            System.out.printf("plik cfg nie istnieje %s%n", cfgFileName);
            System.exit(3);
        }

        Properties props = new Properties();
        props.load(new FileReader(cfgFileName));

        String clientId = props.getProperty("clientId");
        String clientSecret = props.getProperty("clientSecret");

        IdentityPlatfomClient identityPlatfomClient = new IdentityPlatfomClient();
        if (props.getProperty("refreshToken") == null) {
            identityPlatfomClient.getAuthorizationCode("common", clientId);
            Scanner scanner = new Scanner(System.in);
            String code = scanner.nextLine();
            System.out.println("Your string: " + code);
            String refreshToken = identityPlatfomClient.getRefreshToken("common", clientId, clientSecret, code);
            updateRefreshToken(props, cfgFileName, refreshToken);
            System.exit(15);
        }
        String refreshToken = props.getProperty("refreshToken");
        TokenPair tokenPair = identityPlatfomClient.refreshToken(refreshToken, clientId, clientSecret);
        updateRefreshToken(props, cfgFileName, tokenPair.getRefreshToken());

        GraphServiceClient<Request> client = GraphServiceClient.builder().authenticationProvider(
                new TokenCredentialAuthProvider(tokenRequestContext -> Mono.just(new AccessToken(tokenPair.getAccessToken(), OffsetDateTime.MAX)))
        ).buildClient();
        MessageProcessor messageProcessor = new MessageProcessor(client, destFolder);
        //https://docs.microsoft.com/en-us/graph/api/resources/message?view=graph-rest-1.0
        List<Message> messages = Objects.requireNonNull(client.me().mailFolders("Inbox").messages().buildRequest().select("sender,subject").expand("attachments").get()).getCurrentPage();
        messages.forEach(messageProcessor::processMessage);

    }
}
