package utils;

import okhttp3.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttpClient工具类
 * 提供常用的HTTP请求方法封装
 */
public class OkHttpClientUtils {
    
    private static final OkHttpClient client;
    
    static {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 发送GET请求
     * 
     * @param url 请求地址
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    
    /**
     * 发送GET请求（带请求头）
     * 
     * @param url 请求地址
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String get(String url, Map<String, String> headers) throws IOException {
        Request.Builder builder = new Request.Builder().url(url);
        
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        
        Request request = builder.build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    
    /**
     * 发送POST请求（JSON数据）
     * 
     * @param url 请求地址
     * @param json JSON数据
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String postJson(String url, String json) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    
    /**
     * 发送POST请求（JSON数据，带请求头）
     * 
     * @param url 请求地址
     * @param json JSON数据
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String postJson(String url, String json, Map<String, String> headers) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);
        
        Request.Builder builder = new Request.Builder().url(url).post(body);
        
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        
        Request request = builder.build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    
    /**
     * 发送POST请求（表单数据）
     * 
     * @param url 请求地址
     * @param formData 表单数据
     * @return 响应结果
     * @throws IOException IO异常
     */
    public static String postForm(String url, Map<String, String> formData, Map<String, String> headers) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (headers != null){
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        if (formData != null) {
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        
        RequestBody formBody = formBuilder.build();
        
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    
    /**
     * 获取原始的OkHttpClient实例
     * 
     * @return OkHttpClient实例
     */
    public static OkHttpClient getClient() {
        return client;
    }
}
