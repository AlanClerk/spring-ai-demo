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
        log.info("========== HTTP请求详情 ==========");
        log.info("请求方法: {}", request.getMethod());
        log.info("请求URL: {}", request.getURI());
        
        // 记录请求头
        log.info("---------- 请求头 ----------");
        request.getHeaders().forEach((name, values) -> {
            values.forEach(value -> {
                // 脱敏处理API Key
                if ("Authorization".equalsIgnoreCase(name)) {
                    log.info("{}: Bearer {}****", name, 
                            value.length() > 20 ? value.substring(7, 20) : "***");
                } else {
                    log.info("{}: {}", name, value);
                }
            });
        });
        
        // 记录请求体
        if (body != null && body.length > 0) {
            String requestBody = new String(body, StandardCharsets.UTF_8);
            log.info("---------- 请求体 ----------");
            log.info("{}", formatJson(requestBody));
        }
        
        log.info("===================================");
    }
    
    /**
     * 记录HTTP响应详情
     */
    private void logResponse(ClientHttpResponse response, long duration) throws IOException {
        log.info("========== HTTP响应详情 ==========");
        log.info("响应状态码: {} {}", response.getStatusCode().value(), response.getStatusText());
        log.info("响应耗时: {}ms", duration);
        
        // 记录响应头
        log.info("---------- 响应头 ----------");
        response.getHeaders().forEach((name, values) -> {
            values.forEach(value -> log.info("{}: {}", name, value));
        });
        
        log.info("===================================");
    }
    
    /**
     * 格式化JSON字符串（简单处理）
     */
    private String formatJson(String json) {
        if (json == null || json.isEmpty()) {
            return "";
        }
        
        // 如果太长，截断显示
        if (json.length() > 2000) {
            return json.substring(0, 2000) + "\n... (内容过长，已截断)";
        }
        
        return json;
    }
}

