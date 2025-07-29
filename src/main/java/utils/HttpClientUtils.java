package utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpClientUtils {
    private static final HttpClient client;
    
    static {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(30000)
                .setSocketTimeout(30000)
                .build();
        
        client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
    }

    /**
     * 发送GET请求
     *
     * @param url    地址
     * @param params 参数
     * @param headers 请求头
     * @return 请求结果
     */
    public static String get(String url, Map<String, Object> params, Map<String, String> headers) {
        return request("GET", url, params, headers);
    }

    /**
     * 发送POST请求
     *
     * @param url    地址
     * @param params 参数
     * @param headers 请求头
     * @return 请求结果
     */
    public static String post(String url, Map<String, Object> params, Map<String, String> headers) {
        return request("POST", url, params, headers);
    }
    
    /**
     * 发送HTTP请求
     *
     * @param method 请求方法
     * @param url    地址
     * @param params 参数
     * @param headers 请求头
     * @return 请求结果
     */
    public static String request( String url, String method, Map<String, Object> params, Map<String, String> headers) {
        if (method == null) {
            throw new RuntimeException("请求方法不能为空");
        }

        if (url == null) {
            throw new RuntimeException("url不能为空");
        }

        try {
            HttpRequestBase request;
            
            if ("GET".equalsIgnoreCase(method)) {
                // 构建带参数的URL
                URIBuilder uriBuilder = new URIBuilder(url);
                if (params != null) {
                    for (Map.Entry<String, Object> param : params.entrySet()) {
                        uriBuilder.addParameter(param.getKey(), String.valueOf(param.getValue()));
                    }
                }
                request = new HttpGet(uriBuilder.build());
            } else if ("POST".equalsIgnoreCase(method)) {
                HttpPost postRequest = new HttpPost(url);
                // 设置表单参数
                if (params != null && !params.isEmpty()) {
                    List<NameValuePair> formParams = new ArrayList<>();
                    for (Map.Entry<String, Object> param : params.entrySet()) {
                        formParams.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
                    }
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8);
                    postRequest.setEntity(entity);
                }
                request = postRequest;
            } else {
                throw new RuntimeException("不支持的请求方法: " + method);
            }

            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.setHeader(header.getKey(), header.getValue());
                }
            }

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
            
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("HTTP请求失败", e);
        }
    }

    /**
     * 发送POST请求（JSON格式）
     *
     * @param url  url
     * @param json JSON字符串
     * @param headers 请求头
     * @return 请求结果
     */
    public static String postJson(String url, String json, Map<String, String> headers) {
        try {
            HttpPost postRequest = new HttpPost(url);
            
            // 设置JSON请求体
            if (json != null) {
                StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
                entity.setContentType("application/json");
                postRequest.setEntity(entity);
            }
            
            // 添加请求头
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    postRequest.setHeader(header.getKey(), header.getValue());
                }
            }
            
            // 设置JSON内容类型
            postRequest.setHeader("Content-Type", "application/json; charset=utf-8");
            
            HttpResponse response = client.execute(postRequest);
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
            
        } catch (IOException e) {
            throw new RuntimeException("HTTP请求失败", e);
        }
    }
    
    /**
     * 获取原始的HttpClient实例
     * 
     * @return HttpClient实例
     */
    public static HttpClient getClient() {
        return client;
    }
}
