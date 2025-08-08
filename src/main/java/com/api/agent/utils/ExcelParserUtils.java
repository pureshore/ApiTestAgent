package com.api.agent.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson.JSONObject;
import com.api.agent.controller.TestCaseUtils;
import com.api.agent.dto.TestCase;
import com.api.agent.dto.excel.TestCaseExcelData;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel解析工具类
 * 用于读取Excel文件并转换为TestCase对象
 */
public class ExcelParserUtils {

    /**
     * 读取Excel文件并转换为TestCase对象列表
     *
     * @param fileName Excel文件路径
     * @return TestCase对象列表
     */
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

    /**
     * 通过监听器方式读取Excel文件（适用于大文件）
     *
     * @param fileName Excel文件路径
     * @return TestCase对象列表
     */
    public static List<TestCase> readExcelToTestCasesWithListener(String fileName) {
        List<TestCase> testCases = ListUtils.newArrayList();

        EasyExcel.read(fileName, TestCaseExcelData.class, new TestCaseExcelDataListener(testCases))
                .sheet()
                .doRead();

        return testCases;
    }

    /**
     * 将TestCaseExcelData转换为TestCase对象
     *
     * @param excelData Excel数据对象
     * @return TestCase对象
     */
    private static TestCase convertToTestCase(TestCaseExcelData excelData) {
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

        return testCase;
    }

    /**
     * Excel数据读取监听器
     */
    private static class TestCaseExcelDataListener implements ReadListener<TestCaseExcelData> {
        private final List<TestCase> testCases;

        public TestCaseExcelDataListener(List<TestCase> testCases) {
            this.testCases = testCases;
        }

        @Override
        public void invoke(TestCaseExcelData excelData, AnalysisContext context) {
            // 每读取一行数据就转换并添加到列表中
            TestCase testCase = convertToTestCase(excelData);
            testCases.add(testCase);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 所有数据解析完成后的操作
        }
    }

    /**
     * 读取Excel文件并返回原始的TestCaseExcelData对象列表
     *
     * @param fileName Excel文件路径
     * @return TestCaseExcelData对象列表
     */
    public static List<TestCaseExcelData> readExcelToExcelData(String fileName) {
        return EasyExcel.read(fileName)
                .head(TestCaseExcelData.class)
                .sheet()
                .doReadSync();
    }
}
