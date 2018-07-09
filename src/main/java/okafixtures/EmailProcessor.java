package okafixtures;

import java.util.Map;

public class EmailProcessor {
    public enum RuleLogic {
        ANY,
        ALL
    }

    public interface Processor {
        Map<String, String> rule();

        RuleLogic processorLogic();

        void Process();
    }
}
