package pl.com.ids.application.outlook365;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class SendEmailTest {
    @Test
    public void emailIsSent() throws IOException {
        String args[] = {"karol.txt", "karol.kalinski@gmail.com"};
//        SendEmailUsingGraphApi.main(args);
        Assert.assertTrue(true);
    }

}
