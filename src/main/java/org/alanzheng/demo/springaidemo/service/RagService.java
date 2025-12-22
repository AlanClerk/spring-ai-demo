package org.alanzheng.demo.springaidemo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * RAG服务类
 * 实现检索增强生成功能：根据知识库检索相关文档并生成回答
 */
@Slf4j
@Service
public class RagService {
    
    private static final int DEFAULT_TOP_K = 4;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.0;
    private static final String DEFAULT_SYSTEM_PROMPT =
            """
                    你是一个智能助手，基于提供的知识库内容回答问题。
                    请根据以下知识库内容回答用户的问题。如果知识库中没有相关信息，请如实说明。
                    回答要准确、简洁，并引用知识库中的具体内容。""";
    
    private ChatClient chatClient;
    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    
    @Value("${spring.ai.rag.top-k:4}")
    private int topK;
    
    @Value("${spring.ai.rag.similarity-threshold:0.0}")
    private double similarityThreshold;
    
    @Value("${spring.ai.rag.system-prompt:}")
    private String systemPrompt;
    
    /**
     * 构造函数
     */
    public RagService(@Qualifier("openAiChatModel") @Lazy ChatModel chatModel, 
                     VectorStore vectorStore) {
        Objects.requireNonNull(chatModel, "ChatModel不能为空");
        Objects.requireNonNull(vectorStore, "VectorStore不能为空");
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
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
                    chatClient = ChatClient.builder(chatModel).build();
                    log.info("RagService ChatClient 初始化完成");
                }
            }
        }
        return chatClient;
    }
    
    /**
     * 基于知识库回答问题
     * 
     * @param question 用户问题
     * @return AI回答
     */
    public String answer(String question) {
        return answer(question, topK, similarityThreshold);
    }
    
    /**
     * 基于知识库回答问题（可自定义检索参数）
     * 
     * @param question 用户问题
     * @param topK 检索的文档数量
     * @param similarityThreshold 相似度阈值
     * @return AI回答
     */
    public String answer(String question, int topK, double similarityThreshold) {
        long startTime = System.currentTimeMillis();
        log.info("开始RAG问答，问题: {}，topK: {}，相似度阈值: {}", question, topK, similarityThreshold);
        
        try {
            if (StringUtils.isBlank(question)) {
                throw new IllegalArgumentException("问题不能为空");
            }
            
            // 1. 从向量存储中检索相关文档
            List<Document> relevantDocuments = retrieveDocuments(question, topK, similarityThreshold);
            
            if (relevantDocuments.isEmpty()) {
                log.warn("未检索到相关文档，问题: {}", question);
                return "抱歉，知识库中没有找到与您的问题相关的信息。";
            }
            
            log.info("检索到 {} 个相关文档块", relevantDocuments.size());
            
            // 2. 构建包含知识库内容的提示词
            String context = buildContext(relevantDocuments);
            String finalSystemPrompt = StringUtils.isNotBlank(this.systemPrompt) 
                    ? this.systemPrompt 
                    : DEFAULT_SYSTEM_PROMPT;
            
            // 3. 调用LLM生成回答
            String answer = getChatClient().prompt()
                    .system(finalSystemPrompt)
                    .user("知识库内容：\n" + context + "\n\n用户问题：" + question)
                    .call()
                    .content();
            
            if (StringUtils.isBlank(answer)) {
                throw new RuntimeException("AI返回内容为空");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("RAG问答完成，耗时: {}ms，问题: {}，回答长度: {}", 
                    duration, question, answer.length());
            
            return answer;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("RAG问答失败，耗时: {}ms，问题: {}，错误信息: {}", 
                    duration, question, e.getMessage(), e);
            throw new RuntimeException("RAG问答失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从向量存储中检索相关文档
     * 
     * @param query 查询文本
     * @param topK 返回的文档数量
     * @param similarityThreshold 相似度阈值
     * @return 相关文档列表
     */
    private List<Document> retrieveDocuments(String query, int topK, double similarityThreshold) {
        try {
            // 使用SearchRequest的静态方法创建请求
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .build();

            return vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.error("检索文档失败，查询: {}，错误: {}", query, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * 构建上下文内容
     * 将检索到的文档合并成上下文字符串
     * 
     * @param documents 文档列表
     * @return 上下文字符串
     */
    private String buildContext(List<Document> documents) {
        if (Objects.isNull(documents) || documents.isEmpty()) {
            return "";
        }
        
        return documents.stream()
                .map(doc -> {
                    String content = doc.getText();
                    String source = doc.getMetadata().getOrDefault("source", "未知来源").toString();
                    return String.format("【来源：%s】\n%s", source, content);
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }
    
    /**
     * 检索相关文档（不生成回答）
     * 用于调试和查看检索结果
     * 
     * @param query 查询文本
     * @param topK 返回的文档数量
     * @return 相关文档列表
     */
    public List<Document> searchDocuments(String query, int topK) {
        long startTime = System.currentTimeMillis();
        log.info("开始检索文档，查询: {}，topK: {}", query, topK);
        
        try {
            if (StringUtils.isBlank(query)) {
                throw new IllegalArgumentException("查询文本不能为空");
            }
            
            List<Document> documents = retrieveDocuments(query, topK, similarityThreshold);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("文档检索完成，耗时: {}ms，查询: {}，检索到文档数: {}", 
                    duration, query, documents.size());
            
            return documents;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("文档检索失败，耗时: {}ms，查询: {}，错误信息: {}", 
                    duration, query, e.getMessage(), e);
            throw new RuntimeException("文档检索失败: " + e.getMessage(), e);
        }
    }
}

