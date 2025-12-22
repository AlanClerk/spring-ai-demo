package org.alanzheng.demo.springaidemo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * RestClient配置类
 * 配置HTTP请求拦截器，用于记录请求和响应详情
 */
@Slf4j
@Configuration
public class RestClientConfig {
    
    /**
     * 配置RestClient拦截器
     * 用于记录Spring AI向阿里云发送的HTTP请求详情
     */
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        log.info("初始化RestClient拦截器，用于记录HTTP请求详情");
        
        return restClientBuilder -> restClientBuilder
                .requestFactory(bufferingClientHttpRequestFactory())
                .requestInterceptor(new HttpLoggingInterceptor());
    }
    
    /**
     * 创建支持缓冲的请求工厂
     * 允许多次读取请求和响应体
     */
    private ClientHttpRequestFactory bufferingClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 30秒连接超时
        factory.setReadTimeout(60000);    // 60秒读取超时
        return new BufferingClientHttpRequestFactory(factory);
    }
}

