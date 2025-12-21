package org.alanzheng.demo.springaidemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI Demo应用主类
 * 排除Anthropic自动配置，只使用OpenAI
 */
@SpringBootApplication()
public class SpringAiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiDemoApplication.class, args);
    }
}
