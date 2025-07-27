package core;

import java.util.HashMap;
import java.util.Map;

public class VariableStore {
    private static final Map<String, Object> variables = new HashMap<>();

    public static void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    public static Object getVariable(String key) {
        return variables.get(key);
    }

    public static String replaceVariables(String text) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            text = text.replace("${" + entry.getKey() + "}", entry.getValue().toString());
        }
        return text;
    }
}
