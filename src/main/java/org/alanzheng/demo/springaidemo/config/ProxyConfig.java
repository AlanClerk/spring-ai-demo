package org.alanzheng.demo.springaidemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import jakarta.annotation.PostConstruct;

/**
 * 代理配置类
 * 配置所有HTTP/HTTPS请求通过本地7890端口的VPN代理
 */
@Configuration
public class ProxyConfig implements ApplicationListener<ContextRefreshedEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(ProxyConfig.class);
    
    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 7890;
    
    /**
     * 静态初始化块，确保在类加载时就设置代理
     */
    static {
        configureSystemProxy();
    }
    
    /**
     * 配置系统代理属性
     */
    private static void configureSystemProxy() {
        // 设置HTTP代理
        System.setProperty("http.proxyHost", PROXY_HOST);
        System.setProperty("http.proxyPort", String.valueOf(PROXY_PORT));
        
        // 设置HTTPS代理
        System.setProperty("https.proxyHost", PROXY_HOST);
        System.setProperty("https.proxyPort", String.valueOf(PROXY_PORT));
        
        // 不代理本地地址
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");
    }
    
    /**
     * Bean初始化后确认代理配置
     */
    @PostConstruct
    public void verifyProxyConfig() {
        logger.info("代理配置已生效 - HTTP/HTTPS请求将通过 {}:{} 代理", PROXY_HOST, PROXY_PORT);
        logger.debug("系统属性 - http.proxyHost: {}, http.proxyPort: {}, https.proxyHost: {}, https.proxyPort: {}",
                System.getProperty("http.proxyHost"),
                System.getProperty("http.proxyPort"),
                System.getProperty("https.proxyHost"),
                System.getProperty("https.proxyPort"));
    }
    
    /**
     * 应用上下文刷新事件监听
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("应用上下文已刷新，代理配置已生效");
    }
}

