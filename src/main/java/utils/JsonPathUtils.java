package utils;

import com.jayway.jsonpath.JsonPath;
import java.util.Map;

public class JsonPathUtils {
    public static Object extractValue(String json, String jsonPath) {
        return JsonPath.read(json, jsonPath);
    }
}
