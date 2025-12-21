package org.alanzheng.demo.springaidemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.alanzheng.demo.springaidemo.dto.ChatRequest;
import org.alanzheng.demo.springaidemo.dto.ChatResponse;
import org.alanzheng.demo.springaidemo.service.ChatbotService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

/**
 * 聊天控制器
 * 提供REST API接口与Chatbot交互
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    
    private final ChatbotService chatbotService;
    
    public ChatController(ChatbotService chatbotService) {
        Objects.requireNonNull(chatbotService, "ChatbotService不能为空");
        this.chatbotService = chatbotService;
    }
    
    /**
     * 简单聊天接口
     * 
     * @param message 用户消息
     * @return AI回复
     */
    @GetMapping("/simple")
    public ResponseEntity<String> simpleChat(@RequestParam String message) {
        long startTime = System.currentTimeMillis();
        log.info("收到简单聊天请求，消息: {}", message);
        
        if (StringUtils.isBlank(message)) {
            log.warn("简单聊天请求参数验证失败，消息为空");
            return ResponseEntity.badRequest().body("消息内容不能为空");
        }
        
        try {
            String reply = chatbotService.chat(message);
            long duration = System.currentTimeMillis() - startTime;
            log.info("简单聊天请求处理成功，总耗时: {}ms", duration);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("简单聊天请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 标准聊天接口
     * 
     * @param request 聊天请求
     * @return 聊天响应
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("收到标准聊天请求，消息: {}，对话ID: {}", 
                Objects.nonNull(request) ? request.getMessage() : "null",
                Objects.nonNull(request) ? request.getConversationId() : "null");
        
        // 参数校验
        if (Objects.isNull(request) || StringUtils.isBlank(request.getMessage())) {
            log.warn("标准聊天请求参数验证失败，请求对象或消息为空");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String reply = chatbotService.chat(request.getMessage());
            
            // 生成或使用现有的对话ID
            String conversationId = StringUtils.isNotBlank(request.getConversationId()) 
                    ? request.getConversationId() 
                    : UUID.randomUUID().toString();
            
            ChatResponse response = ChatResponse.builder()
                    .reply(reply)
                    .conversationId(conversationId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("标准聊天请求处理成功，总耗时: {}ms，对话ID: {}", duration, conversationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("标准聊天请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 带系统提示的聊天接口
     * 
     * @param systemPrompt 系统提示词
     * @param message 用户消息
     * @return AI回复
     */
    @PostMapping("/with-prompt")
    public ResponseEntity<ChatResponse> chatWithPrompt(
            @RequestParam(required = false) String systemPrompt,
            @RequestParam String message) {
        
        long startTime = System.currentTimeMillis();
        log.info("收到带系统提示的聊天请求，系统提示: {}，用户消息: {}", systemPrompt, message);
        
        if (StringUtils.isBlank(message)) {
            log.warn("带系统提示的聊天请求参数验证失败，消息为空");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String reply = chatbotService.chatWithSystemPrompt(systemPrompt, message);
            
            String conversationId = UUID.randomUUID().toString();
            ChatResponse response = ChatResponse.builder()
                    .reply(reply)
                    .conversationId(conversationId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("带系统提示的聊天请求处理成功，总耗时: {}ms，对话ID: {}", duration, conversationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("带系统提示的聊天请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

