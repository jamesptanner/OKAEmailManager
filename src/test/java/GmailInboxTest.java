import com.google.api.services.gmail.model.Message;
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
    private final String newLabel = "NewLabelString";

    @Before
    public void setup() {

        l.info("Starting tests. Setting up inbox connection");
        GmailInbox.clearCredentials();
        try {
            inbox = new GmailInbox();
            inbox.deleteLabel(newLabel);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {

        try {
            //inbox.getEmailSubjects();
            List<Message> notProcessed = inbox.getMessagesByQuery("-label:Dealt With");
            List<Message> processed = inbox.getMessagesByQuery("label:Dealt With");
            List<Message> all = inbox.getAllMessages();
            Assert.assertNotNull("We have emails we have dealt with", processed);
            Assert.assertNotNull("We have emails we haven't dealt with", notProcessed);
            Assert.assertNotNull("We have all emails", all);
            Assert.assertEquals("total message add up.", all.size(), notProcessed.size() + processed.size());
        } catch (Exception e) {
            Assert.fail(e.getMessage());

        }
    }

    @Test
    public void labels() {
        l.info("Testing Labels");
        List<String> labels = inbox.listLabels();
        Assert.assertTrue("Check that we can retrieve message labels", !labels.isEmpty());
        final int currentLabelCount = labels.size();
        inbox.createLabel(newLabel);
        List<String> newLabels = inbox.listLabels();
        Assert.assertEquals("We have created a new label", newLabels.size(), currentLabelCount + 1);
        Assert.assertTrue("The new label is in the list.", newLabels.contains(newLabel));
        inbox.deleteLabel(newLabel);
        newLabels = inbox.listLabels();
        Assert.assertEquals("We have removed a new label", newLabels.size(), currentLabelCount);
        Assert.assertTrue("The new label is not in the list.", !newLabels.contains(newLabel));

    }
}
