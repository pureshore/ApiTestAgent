package com.api.agent.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurlParserUtils {

    public static final String METHOD_KEY = "method";
    public static final String URL_KEY = "url";
    public static final String HEADERS_KEY = "headers";
    public static final String BODY_KEY = "body";
    public static final String PARAM_TYPE_KEY = "paramType";

    // 预编译正则表达式
    private static final Pattern METHOD_PATTERN = Pattern.compile("--request\\s+(\\w+)|-X\\s+(\\w+)");
    private static final Pattern URL_PATTERN = Pattern.compile("'([^']+)'|\"([^\"]+)\"|(https?://\\S+)");
    private static final Pattern HEADER_PATTERN = Pattern.compile("--header\\s+'([^']+)'|--header\\s+\"([^\"]+)\"|-H\\s+'([^']+)'|-H\\s+\"([^\"]+)\"");
    private static final Pattern DATA_ENCODE_PATTERN = Pattern.compile("--data-urlencode\\s+'([^']+)'|--data-urlencode\\s+\"([^\"]+)\"");
    private static final Pattern DATA_PATTERN = Pattern.compile("--data\\s+'([^']+)'|--data\\s+\"([^\"]+)\"|-d\\s+'([^']+)'|-d\\s+\"([^\"]+)\"");

    public static List<String> splitCurlCommandsSimple(String multiCurlCommands) {
        List<String> curlCommands = new ArrayList<>();

        if (multiCurlCommands == null || multiCurlCommands.trim().isEmpty()) {
            return curlCommands;
        }

        // 直接按换行符分割，然后合并属于同一命令的行
        String[] lines = multiCurlCommands.split("\n");
        StringBuilder currentCommand = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = line.trim();

            // 忽略空行
            if (trimmedLine.isEmpty()) {
                continue;
            }

            // 如果行以"curl"开头且当前命令不为空，则保存当前命令并开始新命令
            if (trimmedLine.startsWith("curl") && currentCommand.length() > 0) {
                curlCommands.add(currentCommand.toString().trim());
                currentCommand = new StringBuilder();
            }

            // 添加当前行到命令中（去掉行末的反斜杠）
            if (trimmedLine.endsWith("\\")) {
                currentCommand.append(trimmedLine, 0, trimmedLine.length() - 1).append(" ");
            } else {
                currentCommand.append(trimmedLine).append(" ");
            }
        }

        // 添加最后一个命令
        if (currentCommand.length() > 0) {
            curlCommands.add(currentCommand.toString().trim());
        }

        return curlCommands;
    }

    public static Map<String, String> parseCurl(String curlCommand) {
        Map<String, String> result = new LinkedHashMap<>();

        if (curlCommand == null || curlCommand.trim().isEmpty()) {
            result.put(METHOD_KEY, "GET");
            result.put(URL_KEY, "");
            result.put(HEADERS_KEY, "{}");
            result.put(BODY_KEY, "");
            result.put(PARAM_TYPE_KEY, "form");
            return result;
        }

        // 提取方法 (GET/POST等)
        String method = extractMethod(curlCommand);
        result.put(METHOD_KEY, method);

        // 提取URL
        String url = extractUrl(curlCommand);
        result.put(URL_KEY, url);

        // 提取Headers
        Map<String, String> headers = extractHeaders(curlCommand);
        result.put(HEADERS_KEY, JSONObject.toJSONString(headers));

        // 提取请求体
        String body = extractBody(curlCommand);
        if (!body.isEmpty()) {
            result.put(BODY_KEY, body);
            result.put(PARAM_TYPE_KEY, "json");
        } else {
            // 检查是否有urlencode数据
            String urlEncodedData = extractUrlEncodedData(curlCommand);
            if (!urlEncodedData.isEmpty()) {
                result.put(BODY_KEY, urlEncodedData);
                result.put(PARAM_TYPE_KEY, "form");
            } else {
                result.put(BODY_KEY, "");
                result.put(PARAM_TYPE_KEY, "form");
            }
        }

        return result;
    }

    private static String extractMethod(String curlCommand) {
        Matcher methodMatcher = METHOD_PATTERN.matcher(curlCommand);
        if (methodMatcher.find()) {
            // 检查哪个组有匹配值
            for (int i = 1; i <= methodMatcher.groupCount(); i++) {
                if (methodMatcher.group(i) != null) {
                    return methodMatcher.group(i);
                }
            }
        }
        return "GET";
    }

    private static String extractUrl(String curlCommand) {
        // 跳过curl命令本身
        String commandWithoutCurl = curlCommand.replaceFirst("curl\\s+", "");

        Matcher urlMatcher = URL_PATTERN.matcher(commandWithoutCurl);
        while (urlMatcher.find()) {
            // 检查哪个组有匹配值
            for (int i = 1; i <= urlMatcher.groupCount(); i++) {
                if (urlMatcher.group(i) != null &&
                    (urlMatcher.group(i).startsWith("http://") || urlMatcher.group(i).startsWith("https://"))) {
                    return urlMatcher.group(i);
                }
            }
        }

        // 如果没有找到http/https开头的URL，尝试找第一个引号包裹的字符串
        Matcher generalUrlMatcher = URL_PATTERN.matcher(commandWithoutCurl);
        if (generalUrlMatcher.find()) {
            for (int i = 1; i <= generalUrlMatcher.groupCount(); i++) {
                if (generalUrlMatcher.group(i) != null) {
                    return generalUrlMatcher.group(i);
                }
            }
        }

        return "";
    }

    private static Map<String, String> extractHeaders(String curlCommand) {
        Map<String, String> headers = new LinkedHashMap<>();
        Matcher headerMatcher = HEADER_PATTERN.matcher(curlCommand);

        while (headerMatcher.find()) {
            // 检查哪个组有匹配值
            String headerStr = null;
            for (int i = 1; i <= headerMatcher.groupCount(); i++) {
                if (headerMatcher.group(i) != null) {
                    headerStr = headerMatcher.group(i);
                    break;
                }
            }

            if (headerStr != null) {
                String[] headerParts = headerStr.split(":", 2);
                if (headerParts.length == 2) {
                    headers.put(headerParts[0].trim(), headerParts[1].trim());
                }
            }
        }
        return headers;
    }

    private static String extractBody(String curlCommand) {
        Matcher dataMatcher = DATA_PATTERN.matcher(curlCommand);
        if (dataMatcher.find()) {
            // 检查哪个组有匹配值
            for (int i = 1; i <= dataMatcher.groupCount(); i++) {
                if (dataMatcher.group(i) != null) {
                    return dataMatcher.group(i);
                }
            }
        }
        return "";
    }

    private static String extractUrlEncodedData(String curlCommand) {
        // 使用LinkedHashMap保持插入顺序并自动去重
        Map<String, String> params = new LinkedHashMap<>();
        Matcher dataEncodeMatcher = DATA_ENCODE_PATTERN.matcher(curlCommand);

        while (dataEncodeMatcher.find()) {
            // 检查哪个组有匹配值
            String dataStr = null;
            for (int i = 1; i <= dataEncodeMatcher.groupCount(); i++) {
                if (dataEncodeMatcher.group(i) != null) {
                    dataStr = dataEncodeMatcher.group(i);
                    break;
                }
            }

            if (dataStr != null) {
                // 解析键值对
                String[] keyValue = dataStr.split("=", 2);
                if (keyValue.length == 2) {
                    // 如果key已存在，新值会覆盖旧值，实现去重
                    params.put(keyValue[0], keyValue[1]);
                } else {
                    // 如果没有等号，当作key，value为空
                    params.put(dataStr, "");
                }
            }
        }

        // 构建结果字符串
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                result.append("&");
            }
            result.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }

        return result.toString();
    }
}
