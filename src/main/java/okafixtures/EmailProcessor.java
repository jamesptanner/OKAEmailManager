package okafixtures;

import com.google.api.client.util.ArrayMap;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

public class EmailProcessor {
    private Logger l = LoggerFactory.getLogger(this.getClass());
    private ArrayList<Processor> processors = new ArrayList<>();

    public boolean addProcessor(JSONObject processor) {
        //figure out the logic
        String logicString = processor.optString("logic");
        RuleLogic logic = RuleLogic.ANY;
        if (!logicString.isEmpty()) {
            try {
                logic = RuleLogic.valueOf(logicString.toUpperCase());
            } catch (IllegalArgumentException e) {
                l.error("Could not identify rule logic:" + logicString);
                return false;
            }

        }
        //figure out the rules
        JSONObject rules = processor.optJSONObject("rules");
        ArrayMap<RuleTarget, String> rulesMap = new ArrayMap<>();
        if (rules != null && rules.length() > 0) {
            for (String key : rules.keySet()) {
                RuleTarget target;
                try {
                    target = RuleTarget.valueOf(key.toUpperCase());
                } catch (IllegalArgumentException e) {
                    l.error("Could not process rule target: " + key);
                    return false;
                }

                // we have a target so lets store the rule.
                rulesMap.put(target, rules.optString(key));
            }
        }
        if (rulesMap.size() == 0) {
            l.error("No rules to store ");

            return false;
        }

        //figure out the response
        JSONObject actions = processor.optJSONObject("actions");
        ArrayMap<Action, String> actionMap = new ArrayMap<>();
        if (actions != null && actions.length() > 0) {
            for (String key : actions.keySet()) {
                Action target;
                try {
                    target = Action.valueOf(key.toUpperCase());
                } catch (IllegalArgumentException e) {
                    l.error("Could not process rule action: " + key);
                    return false;
                }

                // we have a target so lets store the rule.
                actionMap.put(target, rules.optString(key));
            }
        }
        if (actionMap.size() == 0) {
            l.error("No rule action to store ");

            return false;
        }

        processors.add(new Processor(rulesMap, logic, actionMap));
        return true;

    }

    private enum RuleLogic {
        ANY,
        ALL
    }

    private enum RuleTarget {
        SENDER,
        SUBJECT,
        HAS_ATTACHMENT,
        ATTACHMENT_TYPE
    }

    private enum Action {
        EMAIL_FORWARD,
        EMAIL_DELETE,
        EMAIL_LABEL,
        EMAIL_RESPOND
    }

    public class Processor {
        final Map<RuleTarget, String> m_rules;
        final RuleLogic m_logic;
        final Map<Action, String> m_actions;

        Processor(Map<RuleTarget, String> rules, RuleLogic logic, Map<Action, String> actions) {
            m_rules = rules;
            m_logic = logic;
            m_actions = actions;
        }

        private boolean shouldRunRule() {
            int matches = 0;
            for (RuleTarget target : m_rules.keySet()) {
                String rule = m_rules.get(target);
                switch (target) {
                    case SENDER: {

                    }
                    break;
                    case SUBJECT: {

                    }
                    break;
                    case HAS_ATTACHMENT: {

                    }
                    break;
                    case ATTACHMENT_TYPE: {

                    }
                }

            }
            return (m_logic == RuleLogic.ALL) ? m_rules.size() == matches : matches > 0;
        }
    }
}
