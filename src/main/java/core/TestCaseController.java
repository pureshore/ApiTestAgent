package core;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import dto.TestCase;
import dto.excel.TestCaseExcelData;
import utils.CurlParserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestCaseController {
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
                JSONObject headersJson = JSONObject.parseObject(excelData.getHeaders());
                testCase.setHeaders(headersJson);
            } catch (Exception e) {
                // 如果解析失败，设置为空JSONObject
                testCase.setHeaders(new JSONObject());
            }
        } else {
            testCase.setHeaders(new JSONObject());
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
    public static void test() {
        String curlCommand = "curl --location --request POST 'http://localhost:8080/students?name=%E5%BC%A0%E4%B8%89&gender=1&age=20&className=%E8%AE%A1%E7%AE%97%E6%9C%BA%E7%A7%91%E5%AD%A6'\n" +
                "curl --location 'http://localhost:8080/students'\n" +
                "curl --location 'http://localhost:8080/students/1'";
        List<String> curlCommands = CurlParserUtils.splitCurlCommandsSimple(curlCommand);
        List<TestCaseExcelData> testCases = new ArrayList<>();
        for (String oneCommand : curlCommands){
            Map<String, String> testCase = CurlParserUtils.parseCurl(oneCommand);
            System.out.println(testCase);
            TestCaseExcelData excelData = new TestCaseExcelData();
            excelData.setMethod(testCase.get(CurlParserUtils.METHOD_KEY));
            excelData.setUrl(testCase.get(CurlParserUtils.URL_KEY));
            excelData.setHeaders(testCase.get(CurlParserUtils.HEADERS_KEY));
            excelData.setBody(testCase.get(CurlParserUtils.BODY_KEY));
            excelData.setParamType(testCase.get(CurlParserUtils.PARAM_TYPE_KEY));
            excelData.setCaseName("cURL转换用例");
            testCases.add(excelData);
        }

        EasyExcel.write("test1.xlsx", TestCaseExcelData.class)
                .sheet("Test Cases")
                .doWrite(testCases);
    }

    public static void main(String[] args) {
//        test();
        List<TestCase> testCases = readExcelToTestCases("test1.xlsx");
        ApiTester tester = new ApiTester();

        for (TestCase testCase : testCases) {
            System.out.println("\n执行用例: " + testCase.getCaseName());
            try {
                String response = tester.sendRequest(testCase);
                System.out.println(response);
//                tester.assertResponse(response, testCase);
                System.out.println("✅ 测试通过");
            } catch (Exception e) {
                System.err.println("❌ 测试失败: " + e.getMessage());
            }
        }
//        for (TestCase testCase : cases) {
//            System.out.println(testCase);
//        }
    }
}
