import com.google.api.client.util.ArrayMap;
import okafixtures.EmailProcessor;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProcessorTest {
    private EmailProcessor processor;
    private Logger l = Logger.getLogger(this.getClass());

    @Before
    public void setup() {
        processor = new EmailProcessor();
    }

    @Test
    public void CreateProcessor() {
        ArrayMap<String, Boolean> testProcessors = new ArrayMap<>();
        testProcessors.add("{'logic':'ANY', 'rules':{has_attachment:false,sender:blah}}", true);
        testProcessors.add("{'rules':{has_attachment:false,sender:blah}}", true);
        testProcessors.add("{'logic':'ANY'}", false);
        testProcessors.add("{'logic':'PINEAPPLE',rules : {has_attachment:false}}", false);
        testProcessors.add("{rules : {has_coconuts:false}}", false);
        int i = 0;
        for (String json : testProcessors.keySet()) {
            Assert.assertEquals("testing processor example " + ++i, processor.addProcessor(new JSONObject(json)), testProcessors.get(json));
        }
    }

}
