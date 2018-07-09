import okafixtures.GmailInbox;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class GmailInboxTest {
    private static final Logger l = LoggerFactory.getLogger(GmailInboxTest.class);
    private GmailInbox inbox;

    @Before
    public void setup() {

        l.info("Starting tests. Setting up inbox connection");
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
            inbox.getEmailSubjects();
        } catch (Exception e) {
            Assert.fail(e.getMessage());

        }
    }

    @Test
    public void labels() {
        l.info("Testing Labels");
        List<String> labels = inbox.listLabels();
        Assert.assertTrue("Check that we can retrieve message labels", !labels.isEmpty());
    }
}
