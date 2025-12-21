package org.alanzheng.demo.springaidemo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Chatbot服务类
 * 使用Spring AI的ChatClient进行对话
 */
@Slf4j
@Service
public class ChatbotService {
    
    private static final int MAX_LOG_MESSAGE_LENGTH = 200;
    
    private final ChatClient chatClient;
    
    /**
     * 构造函数注入ChatModel，并创建ChatClient
     * 使用@Qualifier指定使用OpenAI的ChatModel
     */
    public ChatbotService(@Qualifier("openAiChatModel") ChatModel chatModel) {
        Objects.requireNonNull(chatModel, "ChatModel不能为空");
        this.chatClient = ChatClient.builder(chatModel).build();
    }
    
    /**
     * 发送消息并获取AI回复
     * 
     * @param message 用户消息
     * @return AI回复内容
     */
    public String chat(String message) {
        long startTime = System.currentTimeMillis();
        String truncatedMessage = truncateMessage(message);
        
        log.info("开始调用chat方法，用户消息: {}", truncatedMessage);
        
        try {
            if (StringUtils.isBlank(message)) {
                throw new IllegalArgumentException("消息内容不能为空");
            }
            
            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
            
            // 判空处理
            if (StringUtils.isBlank(response)) {
                throw new RuntimeException("AI返回内容为空");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            String truncatedResponse = truncateMessage(response);
            log.info("chat方法调用成功，耗时: {}ms，AI回复: {}", duration, truncatedResponse);
            
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("chat方法调用失败，耗时: {}ms，用户消息: {}，错误信息: {}", 
                    duration, truncatedMessage, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 带系统提示的聊天
     * 
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @return AI回复内容
     */
    public String chatWithSystemPrompt(String systemPrompt, String userMessage) {
        long startTime = System.currentTimeMillis();
        String truncatedSystemPrompt = truncateMessage(systemPrompt);
        String truncatedUserMessage = truncateMessage(userMessage);
        
        log.info("开始调用chatWithSystemPrompt方法，系统提示: {}，用户消息: {}", 
                truncatedSystemPrompt, truncatedUserMessage);
        
        try {
            if (StringUtils.isBlank(userMessage)) {
                throw new IllegalArgumentException("消息内容不能为空");
            }
            
            ChatClient.ChatClientRequestSpec spec = chatClient.prompt()
                    .user(userMessage);
            
            // 如果有系统提示词，则添加
            if (StringUtils.isNotBlank(systemPrompt)) {
                spec = spec.system(systemPrompt);
            }
            
            String response = spec.call().content();
            
            // 判空处理
            if (StringUtils.isBlank(response)) {
                throw new RuntimeException("AI返回内容为空");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            String truncatedResponse = truncateMessage(response);
            log.info("chatWithSystemPrompt方法调用成功，耗时: {}ms，AI回复: {}", 
                    duration, truncatedResponse);
            
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("chatWithSystemPrompt方法调用失败，耗时: {}ms，系统提示: {}，用户消息: {}，错误信息: {}", 
                    duration, truncatedSystemPrompt, truncatedUserMessage, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 截断消息内容用于日志记录，避免日志过长
     * 
     * @param message 原始消息
     * @return 截断后的消息
     */
    private String truncateMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return "";
        }
        if (message.length() <= MAX_LOG_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_LOG_MESSAGE_LENGTH) + "...(已截断)";
    }
}

