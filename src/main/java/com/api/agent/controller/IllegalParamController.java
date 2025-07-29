package com.api.agent.controller;

import com.api.agent.dto.TestCase;
import com.api.agent.dto.entity.Student;
import com.api.agent.dto.excel.TestCaseExcelData;
import com.api.agent.utils.CurlParserUtils;
import com.api.agent.utils.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

@Controller
public class IllegalParamController {
    @PostMapping
    public String callError(@RequestParam String curlCommand,
                            @RequestParam("file") MultipartFile excelFile) {
        Map<String, String> basicCase = CurlParserUtils.parseCurl(curlCommand);
        String filePath = FileUtils.convertMultipartToFile(excelFile);
        List<TestCase> testCases = TestCaseUtils.readExcelToTestCases(filePath);
        for (TestCase testCase : testCases){
            testCase.setUrl(basicCase.get(CurlParserUtils.URL_KEY));
            testCase.setMethod(basicCase.get(CurlParserUtils.METHOD_KEY));
            testCase.setErrorBody(testCase.getBody());
            testCase.setBody(basicCase.get(CurlParserUtils.BODY_KEY));
            testCase.setParamType(basicCase.get(CurlParserUtils.PARAM_TYPE_KEY));
//            testCase.setHeaders( basicCase.get(CurlParserUtils.HEADERS_KEY));
        }

        return "执行成功";
    }
}
