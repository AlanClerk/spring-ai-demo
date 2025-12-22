package org.alanzheng.demo.springaidemo.service;

import lombok.extern.slf4j.Slf4j;
import org.alanzheng.demo.springaidemo.mcp.McpTools;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Agent 服务类
 * 集成 MCP 工具，让 AI Agent 能够自动调用工具完成任务
 */
@Slf4j
@Service
public class AgentService {
    
    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是一个智能助手，可以使用以下工具来帮助用户：
            
            1. loadAllDocuments - 加载知识库中的所有文档到向量存储。返回加载的文档数量。
            2. loadDocument - 加载指定文件到向量存储。参数：filePath - 文件的完整路径。返回加载的文档数量。
            3. answerQuestion - 基于知识库回答用户问题。使用RAG（检索增强生成）技术，从知识库中检索相关信息并生成回答。参数：question - 用户的问题。
            4. searchDocuments - 从知识库中检索与查询相关的文档。参数：query - 查询文本；topK - 返回的文档数量（可选，默认4）。返回检索到的文档摘要。
            
            当用户需要查询知识库、加载文档或检索信息时，你应该主动使用相应的工具。
            使用工具后，请根据工具返回的结果给用户一个清晰的回答。
            """;
    
    private ChatClient chatClient;
    private final ChatModel chatModel;
    private final McpTools mcpTools;
    
    @Value("${spring.ai.agent.system-prompt:}")
    private String customSystemPrompt;
    
    /**
     * 构造函数
     * 
     * @param chatModel ChatModel 实例
     * @param mcpTools MCP 工具实例
     */
    public AgentService(@Qualifier("openAiChatModel") ChatModel chatModel,
                       @Lazy McpTools mcpTools) {
        Objects.requireNonNull(chatModel, "ChatModel不能为空");
        Objects.requireNonNull(mcpTools, "McpTools不能为空");
        
        this.chatModel = chatModel;
        this.mcpTools = mcpTools;
        
        log.info("AgentService 初始化完成");
    }
    
    /**
     * 获取或初始化 ChatClient
     * 延迟初始化以避免循环依赖问题
     * 
     * @return ChatClient 实例
     */
    private ChatClient getChatClient() {
        if (chatClient == null) {
            synchronized (this) {
                if (chatClient == null) {
                    // 先强制初始化 mcpTools，确保 @Tool 方法可以被扫描到
                    // 通过访问 hashCode() 来触发懒加载代理的实际初始化
                    if (mcpTools != null) {
                        try {
                            // 触发代理对象的初始化，确保 McpTools 已经被创建
                            // 访问 hashCode() 会触发代理对象的实际初始化
                            mcpTools.hashCode();
                        } catch (Exception e) {
                            log.warn("初始化 McpTools 时出现异常", e);
                        }
                    }
                    
                    // 在运行时创建 ToolCallbackProvider，此时 mcpTools 已经初始化
                    ToolCallbackProvider toolCallbackProvider = MethodToolCallbackProvider.builder()
                            .toolObjects(mcpTools)
                            .build();
                    
                    // 构建 ChatClient，集成工具支持
                    // 通过 ToolCallbackProvider 注册工具
                    chatClient = ChatClient.builder(chatModel)
                            .defaultTools(toolCallbackProvider)
                            .build();
                    log.info("ChatClient 初始化完成，已集成 MCP 工具");
                }
            }
        }
        return chatClient;
    }
    
    /**
     * Agent 对话接口
     * Agent 会根据用户需求自动调用相应的工具
     * 
     * @param message 用户消息
     * @return Agent 回复
     */
    public String chat(String message) {
        long startTime = System.currentTimeMillis();
        log.info("Agent 收到用户消息: {}", truncateMessage(message));
        
        try {
            if (StringUtils.isBlank(message)) {
                throw new IllegalArgumentException("消息内容不能为空");
            }
            
            String systemPrompt = StringUtils.isNotBlank(customSystemPrompt) 
                    ? customSystemPrompt 
                    : DEFAULT_SYSTEM_PROMPT;
            
            // Agent 会自动根据用户需求调用工具
            String response = getChatClient().prompt()
                    .system(systemPrompt)
                    .user(message)
                    .call()
                    .content();
            
            if (StringUtils.isBlank(response)) {
                throw new RuntimeException("Agent返回内容为空");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Agent 处理完成，耗时: {}ms，回复: {}", duration, truncateMessage(response));
            
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Agent 处理失败，耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Agent 对话接口（带自定义系统提示）
     * 
     * @param systemPrompt 自定义系统提示词
     * @param message 用户消息
     * @return Agent 回复
     */
    public String chatWithSystemPrompt(String systemPrompt, String message) {
        long startTime = System.currentTimeMillis();
        log.info("Agent 收到用户消息（自定义提示）: {}", truncateMessage(message));
        
        try {
            if (StringUtils.isBlank(message)) {
                throw new IllegalArgumentException("消息内容不能为空");
            }
            
            String finalSystemPrompt = StringUtils.isNotBlank(systemPrompt) 
                    ? systemPrompt 
                    : DEFAULT_SYSTEM_PROMPT;
            
            String response = getChatClient().prompt()
                    .system(finalSystemPrompt)
                    .user(message)
                    .call()
                    .content();
            
            if (StringUtils.isBlank(response)) {
                throw new RuntimeException("Agent返回内容为空");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Agent 处理完成（自定义提示），耗时: {}ms", duration);
            
            return response;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Agent 处理失败（自定义提示），耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            throw e;
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
}

