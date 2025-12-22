package org.alanzheng.demo.springaidemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.alanzheng.demo.springaidemo.dto.ActorsFilms;
import org.alanzheng.demo.springaidemo.dto.ChatRequest;
import org.alanzheng.demo.springaidemo.dto.ChatResponse;
import org.alanzheng.demo.springaidemo.dto.DocumentInfo;
import org.alanzheng.demo.springaidemo.dto.RagRequest;
import org.alanzheng.demo.springaidemo.dto.StructuredResponse;
import org.alanzheng.demo.springaidemo.dto.WeatherInfo;
import org.alanzheng.demo.springaidemo.service.ChatbotService;
import org.alanzheng.demo.springaidemo.service.DocumentService;
import org.alanzheng.demo.springaidemo.service.RagService;
import org.alanzheng.demo.springaidemo.service.StructuredOutputService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    private final RagService ragService;
    private final DocumentService documentService;
    private final VectorStore vectorStore;
    
    public ChatController(ChatbotService chatbotService, 
                         StructuredOutputService structuredOutputService,
                         RagService ragService,
                         DocumentService documentService,
                         VectorStore vectorStore) {
        Objects.requireNonNull(chatbotService, "ChatbotService不能为空");
        Objects.requireNonNull(structuredOutputService, "StructuredOutputService不能为空");
        Objects.requireNonNull(ragService, "RagService不能为空");
        Objects.requireNonNull(documentService, "DocumentService不能为空");
        Objects.requireNonNull(vectorStore, "VectorStore不能为空");
        this.chatbotService = chatbotService;
        this.structuredOutputService = structuredOutputService;
        this.ragService = ragService;
        this.documentService = documentService;
        this.vectorStore = vectorStore;
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
    
    /**
     * RAG问答接口
     * 基于知识库检索并回答问题
     * 
     * @param request RAG请求
     * @return AI回答
     */
    @PostMapping("/rag")
    public ResponseEntity<ChatResponse> ragAnswer(@RequestBody RagRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("收到RAG问答请求，问题: {}", 
                Objects.nonNull(request) ? request.getQuestion() : "null");
        
        if (Objects.isNull(request) || StringUtils.isBlank(request.getQuestion())) {
            log.warn("RAG问答请求参数验证失败，请求对象或问题为空");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String question = request.getQuestion();
            Integer topK = Objects.nonNull(request.getTopK()) ? request.getTopK() : null;
            Double similarityThreshold = Objects.nonNull(request.getSimilarityThreshold()) 
                    ? request.getSimilarityThreshold() : null;
            
            String answer;
            if (Objects.nonNull(topK) && Objects.nonNull(similarityThreshold)) {
                answer = ragService.answer(question, topK, similarityThreshold);
            } else {
                answer = ragService.answer(question);
            }
            
            String conversationId = UUID.randomUUID().toString();
            ChatResponse response = ChatResponse.builder()
                    .reply(answer)
                    .conversationId(conversationId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("RAG问答请求处理成功，总耗时: {}ms，对话ID: {}", duration, conversationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("RAG问答请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 简单RAG问答接口（GET方式）
     * 
     * @param question 用户问题
     * @return AI回答
     */
    @GetMapping("/rag")
    public ResponseEntity<String> ragAnswerSimple(@RequestParam String question) {
        long startTime = System.currentTimeMillis();
        log.info("收到简单RAG问答请求，问题: {}", question);
        
        if (StringUtils.isBlank(question)) {
            log.warn("简单RAG问答请求参数验证失败，问题为空");
            return ResponseEntity.badRequest().body("问题不能为空");
        }
        
        try {
            String answer = ragService.answer(question);
            long duration = System.currentTimeMillis() - startTime;
            log.info("简单RAG问答请求处理成功，总耗时: {}ms", duration);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("简单RAG问答请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("处理请求时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 测试上传接口：上传一句话到向量存储
     * 用于测试向量化和存储功能是否正常
     * 
     * @param text 要上传的文本
     * @return 上传结果
     */
    @PostMapping("/rag/test-upload")
    public ResponseEntity<StructuredResponse<String>> testUploadText(@RequestParam String text) {
        long startTime = System.currentTimeMillis();
        log.info("收到测试上传请求，文本: {}", text);
        
        if (StringUtils.isBlank(text)) {
            log.warn("测试上传请求参数验证失败，文本为空");
            StructuredResponse<String> errorResponse = StructuredResponse.<String>builder()
                    .success(false)
                    .errorMessage("文本内容不能为空")
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            // 创建文档对象
            Document document = new Document(text);
            document.getMetadata().put("source", "测试上传");
            document.getMetadata().put("type", "test");
            document.getMetadata().put("timestamp", System.currentTimeMillis());
            
            // 上传到向量存储（会自动调用阿里云 Embedding API 进行向量化）
            vectorStore.add(Collections.singletonList(document));
            
            StructuredResponse<String> response = StructuredResponse.<String>builder()
                    .data("文本上传成功！已向量化并存储到阿里云")
                    .success(true)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("测试上传请求处理成功，总耗时: {}ms，文本长度: {}", duration, text.length());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("测试上传请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            
            StructuredResponse<String> errorResponse = StructuredResponse.<String>builder()
                    .success(false)
                    .errorMessage("上传失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 加载知识库文档接口
     * 从配置的知识库目录加载所有文档到向量存储
     * 
     * @return 加载的文档块数量
     */
    @PostMapping("/rag/load-documents")
    public ResponseEntity<StructuredResponse<Integer>> loadDocuments() {
        long startTime = System.currentTimeMillis();
        log.info("收到加载知识库文档请求");
        
        try {
            int documentCount = documentService.loadAllDocuments();
            
            StructuredResponse<Integer> response = StructuredResponse.<Integer>builder()
                    .data(documentCount)
                    .success(true)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("加载知识库文档请求处理成功，总耗时: {}ms，加载文档块数: {}", duration, documentCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("加载知识库文档请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            
            StructuredResponse<Integer> errorResponse = StructuredResponse.<Integer>builder()
                    .success(false)
                    .errorMessage("处理请求时发生错误: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 加载单个文档接口
     * 
     * @param filePath 文件路径
     * @return 加载的文档块数量
     */
    @PostMapping("/rag/load-document")
    public ResponseEntity<StructuredResponse<Integer>> loadDocument(@RequestParam String filePath) {
        long startTime = System.currentTimeMillis();
        log.info("收到加载单个文档请求，文件路径: {}", filePath);
        
        if (StringUtils.isBlank(filePath)) {
            log.warn("加载单个文档请求参数验证失败，文件路径为空");
            StructuredResponse<Integer> errorResponse = StructuredResponse.<Integer>builder()
                    .success(false)
                    .errorMessage("文件路径不能为空")
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            int documentCount = documentService.loadDocument(filePath);
            
            StructuredResponse<Integer> response = StructuredResponse.<Integer>builder()
                    .data(documentCount)
                    .success(true)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("加载单个文档请求处理成功，总耗时: {}ms，加载文档块数: {}", duration, documentCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("加载单个文档请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            
            StructuredResponse<Integer> errorResponse = StructuredResponse.<Integer>builder()
                    .success(false)
                    .errorMessage("处理请求时发生错误: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 检索文档接口
     * 根据查询文本检索相关文档（不生成回答）
     * 
     * @param query 查询文本
     * @param topK 返回的文档数量（默认4）
     * @return 相关文档列表
     */
    @GetMapping("/rag/search")
    public ResponseEntity<StructuredResponse<java.util.List<DocumentInfo>>> searchDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "4") int topK) {
        
        long startTime = System.currentTimeMillis();
        log.info("收到文档检索请求，查询: {}，topK: {}", query, topK);
        
        if (StringUtils.isBlank(query)) {
            log.warn("文档检索请求参数验证失败，查询文本为空");
            StructuredResponse<java.util.List<DocumentInfo>> errorResponse = 
                    StructuredResponse.<java.util.List<DocumentInfo>>builder()
                    .success(false)
                    .errorMessage("查询文本不能为空")
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            java.util.List<Document> documents = ragService.searchDocuments(query, topK);
            
            java.util.List<DocumentInfo> documentInfos = documents.stream()
                    .map(doc -> DocumentInfo.builder()
                            .content(doc.getText())
                            .metadata(doc.getMetadata())
                            .source(doc.getMetadata().getOrDefault("source", "未知来源").toString())
                            .build())
                    .toList();
            
            StructuredResponse<java.util.List<DocumentInfo>> response = 
                    StructuredResponse.<java.util.List<DocumentInfo>>builder()
                    .data(documentInfos)
                    .success(true)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("文档检索请求处理成功，总耗时: {}ms，检索到文档数: {}", duration, documentInfos.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("文档检索请求处理失败，总耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            
            StructuredResponse<java.util.List<DocumentInfo>> errorResponse = 
                    StructuredResponse.<java.util.List<DocumentInfo>>builder()
                    .success(false)
                    .errorMessage("处理请求时发生错误: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

