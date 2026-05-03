package pl.com.ids.application.outlook365;

import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import pl.com.ids.io.AugmentedFileWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MessageProcessor {


    private final GraphServiceClient client;
    private final String destFolder;

    MessageProcessor(GraphServiceClient client, String destFolder) {
        this.client = client;
        this.destFolder = destFolder;
    }

    public void processMessage(Message message) {
        String id = message.getId();
        List<Attachment> attachments = message.getAttachments();
        boolean savedAttachment = attachments.stream().allMatch(attachment -> {
            String name = attachment.getName();
            byte[] contentBytes = ((FileAttachment) attachment).getContentBytes();
            if (name == null || contentBytes == null) {
                return false;
            }
            Path pathToFolder = Paths.get(destFolder);
            AugmentedFileWriter fileWriter = new AugmentedFileWriter(false, true);

            try {
                fileWriter.storeFile(pathToFolder.toFile(), attachment.getName(), new ByteArrayInputStream(contentBytes));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(4);
            }
            return true;
        });
        if (savedAttachment && attachments.size() >0 && id != null) {
            client.me().messages().byMessageId(id).delete();
        }

    }
}
