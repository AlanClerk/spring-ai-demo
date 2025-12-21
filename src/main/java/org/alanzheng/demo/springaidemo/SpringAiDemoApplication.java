package org.alanzheng.demo.springaidemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI Demo应用主类
 * 排除Anthropic自动配置，只使用OpenAI
 */
@SpringBootApplication()
public class SpringAiDemoApplication {

    /**
     * 代理配置常量
     */
    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 7890;

    public static void main(String[] args) {
        // 在应用启动前就配置代理，确保所有HTTP/HTTPS请求通过VPN代理
        configureProxy();
        
        SpringApplication.run(SpringAiDemoApplication.class, args);
    }
    
    /**
     * 配置系统代理属性
     * 所有HTTP/HTTPS请求将通过本地7890端口的VPN代理
     */
    private static void configureProxy() {
        System.setProperty("http.proxyHost", PROXY_HOST);
        System.setProperty("http.proxyPort", String.valueOf(PROXY_PORT));
        System.setProperty("https.proxyHost", PROXY_HOST);
        System.setProperty("https.proxyPort", String.valueOf(PROXY_PORT));
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");
        
        System.out.println("已配置HTTP/HTTPS代理: " + PROXY_HOST + ":" + PROXY_PORT);
    }
}
