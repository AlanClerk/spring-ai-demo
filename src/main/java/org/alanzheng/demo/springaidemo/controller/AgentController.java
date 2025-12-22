package org.alanzheng.demo.springaidemo.controller;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.alanzheng.demo.springaidemo.dto.AgentRequest;
import org.alanzheng.demo.springaidemo.dto.ChatResponse;
import org.alanzheng.demo.springaidemo.service.AgentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

/**
 * Agent 控制器
 * 提供 Agent + MCP 工具的 REST API 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
public class AgentController {
    
    private final AgentService agentService;
    
    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }
    
    /**
     * Agent 简单对话接口（GET方式）
     * 
     * @param message 用户消息
     * @return Agent 回复
     */
    @GetMapping("/chat")
    public ResponseEntity<String> simpleChat(@RequestParam String message) {
        long startTime = System.currentTimeMillis();
        log.info("收到Agent简单对话请求，消息: {}", truncateMessage(message));
        
        if (StringUtils.isBlank(message)) {
            log.warn("Agent简单对话请求参数验证失败，消息为空");
            return ResponseEntity.badRequest().body("消息内容不能为空");
        }
        
        try {
            String reply = agentService.chat(message);
            long duration = System.currentTimeMillis() - startTime;
            log.info("Agent简单对话请求处理成功，总耗时: {}ms", duration);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Agent简单对话请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * Agent 标准对话接口（POST方式）
     * 
     * @param request Agent 请求
     * @return Agent 响应
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody AgentRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("收到Agent标准对话请求，消息: {}，对话ID: {}", 
                Objects.nonNull(request) ? request.getMessage() : "null",
                Objects.nonNull(request) ? request.getConversationId() : "null");
        
        // 参数校验
        if (Objects.isNull(request) || StringUtils.isBlank(request.getMessage())) {
            log.warn("Agent标准对话请求参数验证失败，请求对象或消息为空");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String reply;
            if (StringUtils.isNotBlank(request.getSystemPrompt())) {
                reply = agentService.chatWithSystemPrompt(request.getSystemPrompt(), request.getMessage());
            } else {
                reply = agentService.chat(request.getMessage());
            }
            
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
            log.info("Agent标准对话请求处理成功，总耗时: {}ms，对话ID: {}", duration, conversationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Agent标准对话请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Agent 带系统提示的对话接口
     * 
     * @param systemPrompt 系统提示词
     * @param message 用户消息
     * @return Agent 回复
     */
    @PostMapping("/chat/with-prompt")
    public ResponseEntity<ChatResponse> chatWithPrompt(
            @RequestParam(required = false) String systemPrompt,
            @RequestParam String message) {
        
        long startTime = System.currentTimeMillis();
        log.info("收到Agent带系统提示的对话请求，系统提示: {}，用户消息: {}", 
                systemPrompt, truncateMessage(message));
        
        if (StringUtils.isBlank(message)) {
            log.warn("Agent带系统提示的对话请求参数验证失败，消息为空");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String reply = agentService.chatWithSystemPrompt(systemPrompt, message);
            
            String conversationId = UUID.randomUUID().toString();
            ChatResponse response = ChatResponse.builder()
                    .reply(reply)
                    .conversationId(conversationId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Agent带系统提示的对话请求处理成功，总耗时: {}ms，对话ID: {}", duration, conversationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Agent带系统提示的对话请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取 Agent 信息
     * 
     * @return Agent 信息
     */
    @GetMapping("/info")
    public ResponseEntity<AgentInfo> getAgentInfo() {
        log.info("收到获取Agent信息请求");
        
        try {
            AgentInfo info = AgentInfo.builder()
                    .name("Spring AI Agent with MCP Tools")
                    .description("集成 MCP 工具的智能 Agent，可以自动调用工具完成任务")
                    .capabilities(java.util.Arrays.asList(
                            "自动调用 MCP 工具",
                            "知识库文档加载",
                            "知识库问答",
                            "文档检索"
                    ))
                    .tools(java.util.Arrays.asList(
                            "loadAllDocuments - 加载所有文档",
                            "loadDocument - 加载指定文档",
                            "answerQuestion - 基于知识库回答问题",
                            "searchDocuments - 检索相关文档"
                    ))
                    .build();
            
            log.info("返回Agent信息");
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("获取Agent信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 截断消息内容用于日志记录
     * 
     * @param message 原始消息
     * @return 截断后的消息
     */
    private String truncateMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return "";
        }
        int maxLength = 200;
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength) + "...(已截断)";
    }
    
    /**
     * Agent 信息
     */
    @Data
    @Builder
    public static class AgentInfo {
        private String name;
        private String description;
        private java.util.List<String> capabilities;
        private java.util.List<String> tools;
    }
}

