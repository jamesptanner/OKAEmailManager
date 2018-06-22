import okafixtures.GmailInbox;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class GmailInboxTest {
    private GmailInbox inbox;

    @Before
    public void setup() {
        GmailInbox.clearCredentials();
        try {
            inbox = new GmailInbox();

        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {

        try {
            List<String> labels = inbox.listLabels();
            inbox.getEmailSubjects();
        } catch (Exception e) {
            Assert.fail(e.getMessage());

        }
    }
}
