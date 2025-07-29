package com.api.agent.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class TestCase {
    private String caseName;
    private String method;
    private String url;
    private String body;
    private JSONObject headers;
    private String paramType;
    private String extractRules;
    private String expectedFields;
    private int expectedStatus;
}
