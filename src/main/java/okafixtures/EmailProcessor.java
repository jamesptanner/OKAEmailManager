package okafixtures;

import com.google.api.client.util.ArrayMap;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

public class EmailProcessor {
    private Logger l = LoggerFactory.getLogger(this.getClass());
    public enum RuleLogic {
        ANY,
        ALL
    }

    private ArrayList<Processor> processors = new ArrayList<>();

    public boolean addProcessor(JSONObject processor) {
        String logicString = processor.optString("logic");
        RuleLogic logic = RuleLogic.ANY;
        if (logicString != null) {
            try {
                logic = RuleLogic.valueOf(logicString.toUpperCase());
            } catch (IllegalArgumentException e) {
                l.warn("Could not identify rule logic:" + logicString);
            }

        }
        JSONObject rules = processor.optJSONObject("rules");
        ArrayMap<RuleTarget, String> rulesMap = new ArrayMap<>();
        for (String key : rules.keySet()) {
            RuleTarget target = null;
            try {
                target = RuleTarget.valueOf(key);
            } catch (IllegalArgumentException e) {
                l.warn("Could not process rule target: " + key);
            }

            if (target != null) {
                // we have a target so lets store the rule.
                rulesMap.put(target, rules.optString(key));
            }
        }
        if (rulesMap.size() == 0) {
            return false;
        }
        processors.add(new Processor(rulesMap, logic));
        return true;

    }


    public enum RuleTarget {
        SENDER,
        SUBJECT,
        HAS_ATTACHMENT,
        ATTACHMENT_TYPE
    }

    public class Processor {
        final Map<RuleTarget, String> m_rules;
        final RuleLogic m_logic;

        Processor(Map<RuleTarget, String> rules, RuleLogic logic) {
            m_rules = rules;
            m_logic = logic;
        }

        void Process() {

        }
    }
}
