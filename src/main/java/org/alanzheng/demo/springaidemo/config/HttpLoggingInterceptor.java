package org.alanzheng.demo.springaidemo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP请求日志拦截器
 * 用于记录Spring AI向外发送的HTTP请求和响应详情
 */
@Slf4j
public class HttpLoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
                                       ClientHttpRequestExecution execution) throws IOException {
        
        // 记录请求信息
        logRequest(request, body);
        
        // 执行请求
        long startTime = System.currentTimeMillis();
        ClientHttpResponse response = execution.execute(request, body);
        long duration = System.currentTimeMillis() - startTime;
        
        // 记录响应信息
        logResponse(response, duration);
        
        return response;
    }
    
    /**
     * 记录HTTP请求详情
     */
    private void logRequest(HttpRequest request, byte[] body) {
        log.info("请求路径: {}", request.getURI());
        
        // 记录请求体
        if (body != null && body.length > 0) {
            String requestBody = new String(body, StandardCharsets.UTF_8);
            log.info("请求体: {}", requestBody);
        }
    }
    
    /**
     * 记录HTTP响应详情
     */
    private void logResponse(ClientHttpResponse response, long duration) throws IOException {
        log.info("响应状态码: {}", response.getStatusCode().value());
        log.info("响应耗时: {}ms", duration);
    }
}

