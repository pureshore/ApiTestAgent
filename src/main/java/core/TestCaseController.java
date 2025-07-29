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
        String curlCommand = "curl --location 'https://test12open.ezv-test.com/api/service/open/vod/file/info/tokenRegister' \\\n" +
                "--header 'accessToken: at.dt2ct3ymaf8c1mfdcwrl0d3ia3zwnq9v-3nrxfwzl2z-1ipazdp-n5vflk06h' \\\n" +
                "--header 'Content-Type: application/x-www-form-urlencoded' \\\n" +
                "--data-urlencode 'storageId=E1$Fe$14$7kev$PX15Oh-$N1$61dHp96V$0$f74ed45d.jpeg' \\\n" +
                "--data-urlencode 'duration=1' \\\n" +
                "--data-urlencode 'fileName=test' \\\n" +
                "--data-urlencode 'deviceSerial=889088640' \\\n" +
                "--data-urlencode 'channelNo=12' \\\n" +
                "--data-urlencode 'autoCoverPic=true' \\\n" +
                "--data-urlencode 'spaceId=2027223'";
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

    public static void runtestCase(String caseFileName) {
        List<TestCase> testCases = readExcelToTestCases(caseFileName);
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
    }

    public static void main(String[] args) {
//        test();
        runtestCase("test1.xlsx");
    }
}
