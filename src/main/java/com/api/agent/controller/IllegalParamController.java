package com.api.agent.controller;

import com.api.agent.core.ApiTester;
import com.api.agent.dto.TestCase;
import com.api.agent.utils.CurlParserUtils;
import com.api.agent.utils.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static com.api.agent.utils.CurlParserUtils.*;

@Controller
public class IllegalParamController {
    private ApiTester tester = new ApiTester();
    @PostMapping("/api/error")
    public String callError(@RequestParam String curlCommand,
                            @RequestParam("file") MultipartFile excelFile) {
        Map<String, String> basicCase = CurlParserUtils.parseCurl(curlCommand);
        String filePath = FileUtils.saveUploadedFile(excelFile);
        List<TestCase> testCases = TestCaseUtils.readExcelToTestCases(filePath);
        for (TestCase testCase : testCases){
            testCase.setUrl(basicCase.get(URL_KEY));
            testCase.setMethod(basicCase.get(METHOD_KEY));
            testCase.setErrorBody(testCase.getBody());
            testCase.setBody(basicCase.get(BODY_KEY));
            testCase.setParamType(basicCase.get(PARAM_TYPE_KEY));
            testCase.setHeaders(TestCaseUtils.getHeaders(basicCase.get(HEADERS_KEY)));
            testCase.setError( true);
            System.out.println("\n执行用例: " + testCase.getCaseName());
            String response = tester.sendRequest(testCase);
            System.out.println(response);
//            testCase.setHeaders( basicCase.get(CurlParserUtils.HEADERS_KEY));
        }

        return "执行成功";
    }
}
