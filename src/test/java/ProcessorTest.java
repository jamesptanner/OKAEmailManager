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
        testProcessors.add("{'logic':'ANY', 'rules':{has_attachment:false,sender:blah}, 'actions':{'email_forward':'nothing in particular'}}", true); //everything filled out
        testProcessors.add("{'logic':'ANY', 'rules':{has_attachment:false,sender:blah}}", false); //missing action
        testProcessors.add("{'logic':'ANY', 'rules':{has_attachment:false,sender:blah}, 'actions':{}}", false); //empty action
        testProcessors.add("{'logic':'ANY', 'rules':{has_attachment:false,sender:blah}, 'actions':{'water_plants':'all'}}", false); //bad action
        testProcessors.add("{'rules':{has_attachment:false,sender:blah}, 'actions':{email_forward:'potato'}}", true); //missing logic
        testProcessors.add("{'logic':'ANY'}", false); //missing rule
        testProcessors.add("{'logic':'ANY', rules:{}}", false); //empty rule
        testProcessors.add("{'logic':'ANY', rules:{'has_octopus':'true'}}", false); //bad rule
        testProcessors.add("{'logic':'PINEAPPLE',rules : {has_attachment:false}}", false);
        testProcessors.add("{rules : {has_coconuts:false}}", false);
        for (String json : testProcessors.keySet()) {
            Assert.assertEquals("testing processor example " + json, processor.addProcessor(new JSONObject(json)), testProcessors.get(json));
        }
    }

}
