package com.api.agent.controller;

import com.api.agent.core.ApiTester;
import com.api.agent.dto.TestCase;
import com.api.agent.utils.CurlParserUtils;
import com.api.agent.utils.FileUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.api.agent.utils.CurlParserUtils.*;

@RestController
public class IllegalParamController {
    private ApiTester tester = new ApiTester();
    @PostMapping("/api/error")
    public String callError(@RequestParam String curlCommand,
                            @RequestParam("file") MultipartFile excelFile) {
        Map<String, String> basicCase = CurlParserUtils.parseCurl(curlCommand);
        String filePath = FileUtils.saveUploadedFile(excelFile);
        List<TestCase> testCases = TestCaseUtils.readExcelToTestCases(filePath);
        StringBuffer result = new StringBuffer();
        for (TestCase testCase : testCases){
            testCase.setUrl(basicCase.get(URL_KEY));
            testCase.setMethod(basicCase.get(METHOD_KEY));
            testCase.setErrorBody(testCase.getBody());
            testCase.setBody(basicCase.get(BODY_KEY));
            testCase.setParamType(basicCase.get(PARAM_TYPE_KEY));
            testCase.setHeaders(TestCaseUtils.getHeaders(basicCase.get(HEADERS_KEY)));
            testCase.setError( true);
            tester.sendRequest(testCase);
            tester.assertResponse(testCase);
//            System.out.println("\n执行用例: " + testCase.getCaseName());
//            if(testCase.isSuccess()) {
//                System.out.println("✅ 测试通过");
//            }else {
//                System.out.println("❌ 测试失败: " + testCase.getErrorMsg());
//            }
            List<String> joins =  new ArrayList<>();
            joins.add(testCase.getCaseName());
            joins.add(testCase.isSuccess() ? "✅" : "❌ "+testCase.getErrorMsg());
            joins.add(testCase.getResponseBody());
            result.append(joins.stream().collect(Collectors.joining(";")));
            result.append("\n");
        }

        return result.toString();
    }
}
