package com.api.agent.core;

import com.api.agent.dto.TestCase;
import com.api.agent.utils.HttpClientUtils;
import com.api.agent.utils.JsonPathUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApiTester {
    public String sendRequest(TestCase testCase)  {
        // 替换URL中的变量
        String url = VariableStore.replaceVariables(testCase.getUrl());

        // 构建请求
        String result = null;
        Map<String, String> headers = buildHeaders(testCase);
        if ("json".equalsIgnoreCase(testCase.getParamType())) {
            HttpClientUtils.postJson(url, VariableStore.replaceVariables(testCase.getBody()), headers);
        }else if ("form".equalsIgnoreCase(testCase.getParamType())) {
            Map<String, Object> formParams = buildRequestBody(testCase);
            result = HttpClientUtils.request(url, testCase.getMethod(), formParams, headers);
        }

        // 提取变量
        if (StringUtils.isNotBlank(testCase.getExtractRules())) {
            extractVariables(result, testCase.getExtractRules());
        }

        return result;
    }

    private Map<String, Object> buildErrorParams(TestCase testCase) {
        Map<String, Object> errorParams = new HashMap<>();
        // 处理testCase.getBody()中的form-data参数
        if (testCase.getErrorBody() != null && !testCase.getErrorBody().isEmpty()) {
            for (String pair : testCase.getErrorBody().split(",")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    errorParams.put(kv[0], kv[1]);
                }
            }
        }
        return errorParams;
    }

    private Map<String, String> buildHeaders(TestCase testCase) {
        Map<String, String> headers = new HashMap<>();
        if (testCase.getHeaders() != null && !testCase.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> entry : testCase.getHeaders().entrySet()) {
                headers.put(entry.getKey(), entry.getValue());
            }
        }
        if(testCase.isError()) {
            for (Map.Entry<String, Object> entry : buildErrorParams(testCase).entrySet()) {
                if (entry.getValue().toString().equalsIgnoreCase("NULL")) {
                    headers.remove(entry.getKey());
                }else {
                    headers.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }
        return headers;
    }

    private Map<String, Object> buildRequestBody(TestCase testCase) {
        Map<String, Object> formParams = new HashMap<>();
        // 处理testCase.getBody()中的form-data参数
        if (testCase.getBody() != null && !testCase.getBody().isEmpty()) {
            for (String pair : testCase.getBody().split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    formParams.put(kv[0], VariableStore.replaceVariables(kv[1]));
                }
            }
        }

        // 如果是GET请求且URL中有查询参数，也可以提取并放入body
        if (testCase.getUrl().contains("?")) {
            String queryString = testCase.getUrl().substring(testCase.getUrl().indexOf("?") + 1);
            for (String pair : queryString.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    formParams.put(kv[0], VariableStore.replaceVariables(kv[1]));
                }
            }
        }
        if(testCase.isError()) {
            for (Map.Entry<String, Object> entry : buildErrorParams(testCase).entrySet()) {
                if (entry.getValue().toString().equalsIgnoreCase("NULL")) {
                    formParams.remove(entry.getKey());
                }
                formParams.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return formParams;
    }


    private void extractVariables(String json, String extractRules) {
        if (StringUtils.isBlank(extractRules) || StringUtils.isBlank(json)) {
            return;
        }
        try {
            for (String rule : extractRules.split(";")) {
                String[] parts = rule.split("=");
                if (parts.length == 2) {
                    String varName = parts[0].trim();
                    String jsonPath = parts[1].trim();
                    Object value = JsonPathUtils.extractValue(json, jsonPath);
                    VariableStore.setVariable(varName, value);
                }
            }
        } catch (Exception e) {
            System.err.println("变量提取失败: " + e.getMessage());
        }
    }

}