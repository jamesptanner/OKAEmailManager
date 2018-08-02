package okafixtures;

import com.google.api.client.util.ArrayMap;
import com.google.api.services.gmail.model.Message;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmailProcessor {
    private final GmailInbox m_inbox;
    private Logger l = LoggerFactory.getLogger(this.getClass());
    private ArrayList<Processor> processors = new ArrayList<>();

    /**
     * Default constructor that sets the inbox to null. used for testing without processing messages.
     */
    public EmailProcessor() {
        m_inbox = null;
    }

    /**
     * Contructor for the email processor.
     * @param inbox inbox object that is used to perform email actions on.
     */
    EmailProcessor(GmailInbox inbox) {
        m_inbox = inbox;
        List<Message> configMessages = m_inbox.getMessagesByQuery("label:config");
        if (configMessages.size() > 0) {
            for (Message msg : configMessages) {
                msg.getPayload().getBody().getData();
            }
        }
    }

    /**
     * Adds a new processor to the list that will be used to process any messages that arrive.
     * @param processor The JSON object that contains the definition for the processor.
     * @return True if the processor was successfully parsed. Fail if there was a problem.
     */
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

    /**
     * Runs a message agains the processors that are registered in the class.
     * @param msg message to process.
     * @return True fi there was a processor that handled the message, otherwise false.
     */
    public boolean ProcessMessage(Message msg) {
        boolean res = false;
        for (Processor processor : processors) {
            if (processor.shouldRunAction(msg)) {
                res = true;
                processor.runActions(msg);
            }
        }
        return res;
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

    /**
     * Class defining the logic processor.
     */
    public class Processor {
        /**
         * list of rules that the message must match to be handled by this processor object.
         */
        final Map<RuleTarget, String> m_rules;
        /**
         * Indicated if all the rules should match, or at least one.
         */
        final RuleLogic m_logic;
        /**
         * Actions that are performed when the messages matches the rules.
         */
        final Map<Action, String> m_actions;

        /**
         * constructor for the processor.
         *
         * @param rules   list of rules to match against
         * @param logic   whether we match against any or all of the rules.
         * @param actions list of actions to do when the rules are matched correctly
         */
        Processor(Map<RuleTarget, String> rules, RuleLogic logic, Map<Action, String> actions) {
            m_rules = rules;
            m_logic = logic;
            m_actions = actions;
        }

        /**
         * Processses message against rule to see if the actions should run.
         * @param msg The message to process
         * @return True if we should process the message with these actions or false if we do not.
         */
        private boolean shouldRunAction(Message msg) {
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

        /**
         * Runs the actions against the message.
         * @param msg Message that is being processed.
         */
        private void runActions(Message msg) {
            for (Action action : m_actions.keySet()) {
                String args = m_actions.get(action);
                switch (action) {
                    case EMAIL_LABEL: {

                    }
                    break;
                    case EMAIL_DELETE: {

                    }
                    break;
                    case EMAIL_FORWARD: {

                    }
                    break;
                    case EMAIL_RESPOND: {

                    }
                    break;
                }
            }
        }
    }
}
