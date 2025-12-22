package org.alanzheng.demo.springaidemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.alanzheng.demo.springaidemo.dto.ActorsFilms;
import org.alanzheng.demo.springaidemo.dto.ChatRequest;
import org.alanzheng.demo.springaidemo.dto.ChatResponse;
import org.alanzheng.demo.springaidemo.dto.StructuredResponse;
import org.alanzheng.demo.springaidemo.dto.WeatherInfo;
import org.alanzheng.demo.springaidemo.service.ChatbotService;
import org.alanzheng.demo.springaidemo.service.StructuredOutputService;
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
    private final StructuredOutputService structuredOutputService;
    
    public ChatController(ChatbotService chatbotService, 
                         StructuredOutputService structuredOutputService) {
        Objects.requireNonNull(chatbotService, "ChatbotService不能为空");
        Objects.requireNonNull(structuredOutputService, "StructuredOutputService不能为空");
        this.chatbotService = chatbotService;
        this.structuredOutputService = structuredOutputService;
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
    
    /**
     * 结构化输出示例：获取演员及其电影列表
     * 演示Spring AI的基础结构化输出功能
     * 
     * @param actorName 演员姓名
     * @param movieCount 电影数量（默认5部）
     * @return 结构化响应，包含演员及其电影列表
     */
    @GetMapping("/structured/actors-films")
    public ResponseEntity<StructuredResponse<ActorsFilms>> getActorsFilms(
            @RequestParam String actorName,
            @RequestParam(defaultValue = "5") int movieCount) {
        
        long startTime = System.currentTimeMillis();
        log.info("收到获取演员电影列表请求，演员: {}，数量: {}", actorName, movieCount);
        
        if (StringUtils.isBlank(actorName)) {
            log.warn("获取演员电影列表请求参数验证失败，演员姓名为空");
            StructuredResponse<ActorsFilms> errorResponse = StructuredResponse.<ActorsFilms>builder()
                    .success(false)
                    .errorMessage("演员姓名不能为空")
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            ActorsFilms actorsFilms = structuredOutputService.getActorsFilms(actorName, movieCount);
            
            StructuredResponse<ActorsFilms> response = StructuredResponse.<ActorsFilms>builder()
                    .data(actorsFilms)
                    .success(true)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("获取演员电影列表请求处理成功，总耗时: {}ms，演员: {}", duration, actorName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("获取演员电影列表请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            
            StructuredResponse<ActorsFilms> errorResponse = StructuredResponse.<ActorsFilms>builder()
                    .success(false)
                    .errorMessage("处理请求时发生错误: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 结构化输出 + Advisors API示例：获取天气信息
     * 演示Spring AI的结构化输出和Advisors API的结合使用
     * 使用StructuredOutputValidationAdvisor确保输出的有效性，自动重试
     * 
     * @param city 城市名称
     * @param month 月份（1-12）
     * @param maxRetryAttempts 最大重试次数（默认3次）
     * @return 结构化响应，包含天气信息
     */
    @GetMapping("/structured/weather")
    public ResponseEntity<StructuredResponse<WeatherInfo>> getWeatherInfo(
            @RequestParam String city,
            @RequestParam int month,
            @RequestParam(defaultValue = "3") int maxRetryAttempts) {
        
        long startTime = System.currentTimeMillis();
        log.info("收到获取天气信息请求，城市: {}，月份: {}，最大重试次数: {}", city, month, maxRetryAttempts);
        
        if (StringUtils.isBlank(city)) {
            log.warn("获取天气信息请求参数验证失败，城市名称为空");
            StructuredResponse<WeatherInfo> errorResponse = StructuredResponse.<WeatherInfo>builder()
                    .success(false)
                    .errorMessage("城市名称不能为空")
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (month < 1 || month > 12) {
            log.warn("获取天气信息请求参数验证失败，月份无效: {}", month);
            StructuredResponse<WeatherInfo> errorResponse = StructuredResponse.<WeatherInfo>builder()
                    .success(false)
                    .errorMessage("月份必须在1-12之间")
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            WeatherInfo weatherInfo = structuredOutputService.getWeatherInfo(city, month, maxRetryAttempts);
            
            StructuredResponse<WeatherInfo> response = StructuredResponse.<WeatherInfo>builder()
                    .data(weatherInfo)
                    .success(true)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("获取天气信息请求处理成功，总耗时: {}ms，城市: {}，月份: {}，平均温度: {}℃", 
                    duration, weatherInfo.city(), weatherInfo.month(), weatherInfo.averageTemperature());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("获取天气信息请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            
            StructuredResponse<WeatherInfo> errorResponse = StructuredResponse.<WeatherInfo>builder()
                    .success(false)
                    .errorMessage("处理请求时发生错误: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

