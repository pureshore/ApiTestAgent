package com.api.agent.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.api.agent.dto.TestCase;
import com.api.agent.dto.excel.TestCaseExcelData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCaseUtils {
    public static Map<String, String> getHeaders(String headers) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        JSONObject headersJson = JSONObject.parseObject(headers);
        Map<String, String> map = new HashMap<>();
        for (String key : headersJson.keySet()) {
            map.put(key, headersJson.get(key).toString());
        }
        return map;
    }
    /**
     * 将TestCaseExcelData转换为TestCase对象
     *
     * @param excelData Excel数据对象
     * @return TestCase对象
     */
    public static TestCase convertToTestCase(TestCaseExcelData excelData) {
        TestCase testCase = new TestCase();

        // 设置基本信息
        testCase.setCaseName(excelData.getCaseName());
        testCase.setMethod(excelData.getMethod());
        testCase.setUrl(excelData.getUrl());
        testCase.setParamType(excelData.getParamType());
        testCase.setBody(excelData.getBody());

        // 解析Headers
        if (excelData.getHeaders() != null && !excelData.getHeaders().isEmpty()) {
            try {
                testCase.setHeaders(TestCaseUtils.getHeaders(excelData.getHeaders()));
            } catch (Exception e) {
                // 如果解析失败，设置为空JSONObject
                testCase.setHeaders(null);
            }
        }

        // 解析ExpectedResult（如果存在）
        if (excelData.getExpectedFields() != null && !excelData.getExpectedFields().isEmpty()) {
            testCase.setExpectedFields(excelData.getExpectedFields());
        }
        if(excelData.getExtractRules() != null && !excelData.getExtractRules().isEmpty()){
            testCase.setExtractRules(excelData.getExtractRules());
        }
        if (excelData.getExpectedStatus() != null ){
            testCase.setExpectedStatus(excelData.getExpectedStatus());
        }

        return testCase;
    }
    public static List<TestCase> readExcelToTestCases(String fileName) {
        List<TestCase> testCases = new ArrayList<>();

        // 读取Excel文件
        List<TestCaseExcelData> excelDataList = EasyExcel.read(fileName)
                .head(TestCaseExcelData.class)
                .sheet()
                .doReadSync();

        // 转换为TestCase对象
        for (TestCaseExcelData excelData : excelDataList) {
            TestCase testCase = convertToTestCase(excelData);
            testCases.add(testCase);
        }
        return testCases;
    }
}
