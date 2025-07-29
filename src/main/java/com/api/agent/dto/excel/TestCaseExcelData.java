package com.api.agent.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class TestCaseExcelData {
    @ExcelProperty("用例描述")
    private String caseName;

    @ExcelProperty("方法")
    private String method;

    @ExcelProperty("URL")
    private String url;

    @ExcelProperty("请求体")
    private String body;

    @ExcelProperty("参数格式")
    private String paramType;

    @ExcelProperty("请求头")
    private String headers;

    @ExcelProperty("提取变量")
    private String extractRules;

    @ExcelProperty("预期响应字段")
    private String expectedFields;

    @ExcelProperty("预期状态码")
    private Integer expectedStatus;
}
