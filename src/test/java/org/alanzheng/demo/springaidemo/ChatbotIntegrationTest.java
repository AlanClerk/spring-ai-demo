package org.alanzheng.demo.springaidemo;

import org.alanzheng.demo.springaidemo.service.ChatbotService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chatbot集成测试
 */
@SpringBootTest
class ChatbotIntegrationTest {
    
    @Autowired
    private ChatbotService chatbotService;
    
    @Test
    void testChatbotServiceNotNull() {
        assertNotNull(chatbotService, "ChatbotService应该被正确注入");
    }
    
    @Test
    void testSimpleChat() {
        String message = "你好，请用一句话介绍一下Spring框架";
        String response = chatbotService.chat(message);
        
        assertNotNull(response, "响应不应为null");
        assertTrue(StringUtils.isNotBlank(response), "响应内容不应为空");
        System.out.println("用户: " + message);
        System.out.println("AI: " + response);
    }
    
    @Test
    void testChatWithSystemPrompt() {
        String systemPrompt = "你是一个Java编程专家，请用简洁专业的语言回答问题";
        String userMessage = "什么是Spring Boot？";
        
        String response = chatbotService.chatWithSystemPrompt(systemPrompt, userMessage);
        
        assertNotNull(response, "响应不应为null");
        assertTrue(StringUtils.isNotBlank(response), "响应内容不应为空");
        System.out.println("系统提示: " + systemPrompt);
        System.out.println("用户: " + userMessage);
        System.out.println("AI: " + response);
    }
    
    @Test
    void testChatWithEmptyMessage() {
        assertThrows(IllegalArgumentException.class, () -> {
            chatbotService.chat("");
        }, "空消息应该抛出IllegalArgumentException");
    }
    
    @Test
    void testChatWithNullMessage() {
        assertThrows(IllegalArgumentException.class, () -> {
            chatbotService.chat(null);
        }, "null消息应该抛出IllegalArgumentException");
    }
}

