package core;

import dto.TestCase;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import utils.JsonPathUtils;

import java.io.IOException;

public class ApiTester {
    private final OkHttpClient client = new OkHttpClient();

    public String sendRequest(TestCase testCase) throws IOException {
        // 替换URL中的变量
        String url = VariableStore.replaceVariables(testCase.getUrl());

        // 构建请求
        Request.Builder builder = new Request.Builder()
                .url(url);
        RequestBody requestBody = buildRequestBody(testCase);
        if (requestBody != null) {
            builder.method(testCase.getMethod(), requestBody);
        } else {
            builder.method(testCase.getMethod(), null);
        }

        // 发送请求
        Response response = client.newCall(builder.build()).execute();
        String result = response.body().string();

        // 提取变量
        if (StringUtils.isNotBlank(testCase.getExtractRules())) {
            extractVariables(result, testCase.getExtractRules());
        }

        return result;
    }

    private RequestBody buildRequestBody(TestCase testCase) {
        if ("json".equalsIgnoreCase(testCase.getParamType())) {
            String jsonBody = VariableStore.replaceVariables(testCase.getBody());
            return RequestBody.create(jsonBody, MediaType.parse("application/json"));
        } else if ("form".equalsIgnoreCase(testCase.getParamType())) {
            FormBody.Builder formBuilder = new FormBody.Builder();

            // 处理testCase.getBody()中的form-data参数
            if (testCase.getBody() != null && !testCase.getBody().isEmpty()) {
                for (String pair : testCase.getBody().split(";")) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        formBuilder.add(kv[0], VariableStore.replaceVariables(kv[1]));
                    }
                }
            }

            // 如果是GET请求且URL中有查询参数，也可以提取并放入body
            if (testCase.getUrl().contains("?")) {
                String queryString = testCase.getUrl().substring(testCase.getUrl().indexOf("?") + 1);
                for (String pair : queryString.split("&")) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        formBuilder.add(kv[0], VariableStore.replaceVariables(kv[1]));
                    }
                }
            }
            FormBody formBody = formBuilder.build();
            return formBody.size() > 0 ? formBody : null; // 如果没有参数则返回null
        }

        return null;
    }


    private void extractVariables(String json, String extractRules) {
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

    public void assertResponse(Response response, TestCase testCase) {
        // 断言状态码
        if (response.code() != testCase.getExpectedStatus()) {
            throw new AssertionError(
                    "状态码不符: 预期 " + testCase.getExpectedStatus() +
                            ", 实际 " + response.code()
            );
        }

        // 断言响应字段
        if (!testCase.getExpectedFields().isEmpty()) {
            try {
                String json = response.body().string();
                for (String rule : testCase.getExpectedFields().split(";")) {
                    String[] parts = rule.split("=");
                    if (parts.length == 2) {
                        String fieldPath = parts[0].trim();
                        String expectedValue = VariableStore.replaceVariables(parts[1].trim());
                        Object actualValue = JsonPathUtils.extractValue(json, fieldPath);
                        if (!expectedValue.equals(actualValue.toString())) {
                            throw new AssertionError(
                                    "字段不符: " + fieldPath +
                                            " 预期 " + expectedValue +
                                            ", 实际 " + actualValue
                            );
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("断言失败: " + e.getMessage());
            }
        }
    }
}