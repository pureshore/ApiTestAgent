package com.api.agent.utils;

import com.jayway.jsonpath.JsonPath;

public class JsonPathUtils {
    public static Object extractValue(String json, String jsonPath) {
        return JsonPath.read(json, jsonPath);
    }
}
