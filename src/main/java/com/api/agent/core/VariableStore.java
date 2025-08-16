package com.api.agent.core;

import java.util.HashMap;
import java.util.Map;

public class VariableStore {
    private final Map<String, Object> variables = new HashMap<>();

    public void clearAllVariables() {
        variables.clear();
    }

    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    public Object getVariable(String key) {
        key = key.replace("$","");
        return variables.get(key);
    }

    public String replaceVariables(String text) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if(entry.getValue() == null){
                continue;
            }
            text = text.replace("${" + entry.getKey() + "}", entry.getValue().toString());
        }
        return text;
    }
}
