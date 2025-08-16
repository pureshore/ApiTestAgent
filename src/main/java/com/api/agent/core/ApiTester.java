package com.api.agent.core;

import com.api.agent.dto.TestCase;
import com.api.agent.utils.HttpClientUtils;
import com.api.agent.utils.JsonPathUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class ApiTester {
    private VariableStore variableStore = new VariableStore();

    /**
     * 发送请求
     * @param testCase
     */
    public void sendRequest(TestCase testCase)  {
        // 替换URL中的变量
        String url = variableStore.replaceVariables(testCase.getUrl());

        // 构建请求
        Map<String, String> headers = buildHeaders(testCase);
        if ("json".equalsIgnoreCase(testCase.getParamType())) {
            HttpClientUtils.postJson(url, variableStore.replaceVariables(testCase.getBody()), headers);
        }else if ("form".equalsIgnoreCase(testCase.getParamType())) {
            Map<String, Object> formParams = buildRequestBody(testCase);
            HttpResponse response = HttpClientUtils.httpPost(url, testCase.getMethod(), formParams, headers);
            testCase.setHttpCode(200);
            url = url.split("\\?")[0];
            if(headers.containsKey("Content-Type")
                    && headers.get("Content-Type").equalsIgnoreCase("application/x-www-form-urlencoded")){
                testCase.setResponseBody(HttpClientUtils.postFormUrlEncoded(url, formParams, headers));
            }else {
                if(testCase.getMethod().equalsIgnoreCase("POST")){
                    testCase.setResponseBody(HttpClientUtils.post(url, formParams, headers));
                }else if(testCase.getMethod().equalsIgnoreCase("GET")){
                    testCase.setResponseBody(HttpClientUtils.get(url, formParams, headers));
                }
            }
        }
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

    /**
     * 构建请求头
     * @param testCase
     * @return
     */
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
                }else if (entry.getValue().toString().equalsIgnoreCase("Empty")) {
                    headers.put(entry.getKey(), "");
                }
                else {
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
                    formParams.put(kv[0], variableStore.replaceVariables(kv[1]));
                }
            }
        }

        // 如果是GET请求且URL中有查询参数，也可以提取并放入body
        if (testCase.getUrl().contains("?")) {
            String queryString = testCase.getUrl().substring(testCase.getUrl().indexOf("?") + 1);
            for (String pair : queryString.split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    formParams.put(kv[0], variableStore.replaceVariables(kv[1]));
                }
            }
        }
        if(testCase.isError()) {
            for (Map.Entry<String, Object> entry : buildErrorParams(testCase).entrySet()) {
                if (entry.getValue().toString().equalsIgnoreCase("NULL")) {
                    formParams.remove(entry.getKey());
                } else if (entry.getValue().toString().equalsIgnoreCase("Empty")) {
                    formParams.put(entry.getKey(), "");
                }
                formParams.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return formParams;
    }


    /**
     * 提取返回接口的变量
     * @param json
     * @param extractRules
     */
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
                    variableStore.setVariable(varName, null);
                    Object value = JsonPathUtils.extractValue(json, jsonPath);
                    variableStore.setVariable(varName, value);
                }
            }
        } catch (Exception e) {
            System.err.println("变量提取失败: " + e.getMessage());
        }
    }

    /**
     * 断言接口返回
     * @param testCase
     */
    public void assertResponse(TestCase testCase) {
        testCase.setSuccess(true);

        // 断言状态码
        if (testCase.getHttpCode() != testCase.getExpectedStatus()) {
            String errorMsg = "状态码不符: 预期 " + testCase.getExpectedStatus() + ", 实际 " + testCase.getHttpCode();
            testCase.setSuccess(false);
            testCase.setErrorMsg(errorMsg);
        }

        // 断言响应字段
        if (!testCase.getExpectedFields().isEmpty()) {
            try {
                // 提取变量
                if (StringUtils.isNotBlank(testCase.getExtractRules())) {
                    extractVariables(testCase.getResponseBody(), testCase.getExtractRules());
                }

                for (String rule : testCase.getExpectedFields().split(";")) {
                    String[] parts = rule.split("=");
                    if (parts.length == 2) {
                        String fieldPath = parts[0].trim();
                        String expectedValue = parts[1].trim();
                        Object actualValue = variableStore.getVariable(fieldPath);
                        if (!expectedValue.equals(actualValue.toString())) {
                            String errorMsg = "字段 " + fieldPath + " 值不符: 预期 " + expectedValue + ", 实际 " + actualValue;
                            testCase.setSuccess(false);
                            testCase.setErrorMsg(errorMsg);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("断言失败: " + e.getMessage());
            }
        }
        System.out.println(testCase.getResponseBody());
    }

}