package org.alanzheng.demo.springaidemo.mcp;

import lombok.extern.slf4j.Slf4j;
import org.alanzheng.demo.springaidemo.service.DocumentService;
import org.alanzheng.demo.springaidemo.service.RagService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * MCP 工具类
 * 将业务服务功能暴露为 MCP 工具，供 AI Agent 调用
 */
@Slf4j
@Component
public class McpTools {
    
    private final DocumentService documentService;
    private final RagService ragService;
    
    public McpTools(DocumentService documentService, RagService ragService) {
        Objects.requireNonNull(documentService, "DocumentService不能为空");
        Objects.requireNonNull(ragService, "RagService不能为空");
        this.documentService = documentService;
        this.ragService = ragService;
    }
    
    /**
     * 加载知识库文档工具
     * 将知识库中的所有文档加载到向量存储中
     * 
     * @return 加载的文档数量
     */
    @Tool(description = "加载知识库中的所有文档到向量存储。返回加载的文档数量。")
    public String loadAllDocuments() {
        try {
            log.info("MCP工具调用：加载所有文档");
            int count = documentService.loadAllDocuments();
            return String.format("成功加载 %d 个文档到知识库", count);
        } catch (Exception e) {
            log.error("加载文档失败", e);
            return "加载文档失败: " + e.getMessage();
        }
    }
    
    /**
     * 加载指定文档工具
     * 
     * @param filePath 文件路径
     * @return 加载结果
     */
    @Tool(description = "加载指定文件到向量存储。参数：filePath - 文件的完整路径。返回加载的文档数量。")
    public String loadDocument(String filePath) {
        try {
            if (StringUtils.isBlank(filePath)) {
                return "错误：文件路径不能为空";
            }
            
            log.info("MCP工具调用：加载文档，路径: {}", filePath);
            int count = documentService.loadDocument(filePath);
            return String.format("成功加载文件 %s，共 %d 个文档", filePath, count);
        } catch (Exception e) {
            log.error("加载文档失败，路径: {}", filePath, e);
            return "加载文档失败: " + e.getMessage();
        }
    }
    
    /**
     * 基于知识库回答问题工具
     * 
     * @param question 用户问题
     * @return AI回答
     */
    @Tool(description = "基于知识库回答用户问题。使用RAG（检索增强生成）技术，从知识库中检索相关信息并生成回答。参数：question - 用户的问题。")
    public String answerQuestion(String question) {
        try {
            if (StringUtils.isBlank(question)) {
                return "错误：问题不能为空";
            }
            
            log.info("MCP工具调用：回答问题，问题: {}", question);
            String answer = ragService.answer(question);
            
            if (StringUtils.isBlank(answer)) {
                return "抱歉，无法生成回答";
            }
            
            return answer;
        } catch (Exception e) {
            log.error("回答问题失败，问题: {}", question, e);
            return "回答问题失败: " + e.getMessage();
        }
    }
    
    /**
     * 检索知识库文档工具
     * 从向量存储中检索与查询相关的文档
     * 
     * @param query 查询文本
     * @param topK 返回的文档数量，默认为4
     * @return 检索到的文档摘要
     */
    @Tool(description = "从知识库中检索与查询相关的文档。参数：query - 查询文本；topK - 返回的文档数量（可选，默认4）。返回检索到的文档摘要。")
    public String searchDocuments(String query, Integer topK) {
        try {
            if (StringUtils.isBlank(query)) {
                return "错误：查询文本不能为空";
            }
            
            int k = (topK != null && topK > 0) ? topK : 4;
            log.info("MCP工具调用：检索文档，查询: {}，topK: {}", query, k);
            
            List<Document> documents = ragService.searchDocuments(query, k);
            
            if (documents.isEmpty()) {
                return String.format("未找到与 '%s' 相关的文档", query);
            }
            
            // 构建文档摘要
            StringBuilder result = new StringBuilder();
            result.append(String.format("找到 %d 个相关文档：\n\n", documents.size()));
            
            for (int i = 0; i < documents.size(); i++) {
                Document doc = documents.get(i);
                String content = doc.getText();
                String source = doc.getMetadata().getOrDefault("source", "未知来源").toString();
                
                // 截取内容前200个字符作为摘要
                String summary = null;
                if (content != null) {
                    summary = content.length() > 200
                        ? content.substring(0, 200) + "..."
                        : content;
                }

                result.append(String.format("文档 %d:\n", i + 1));
                result.append(String.format("来源: %s\n", source));
                result.append(String.format("内容摘要: %s\n\n", summary));
            }
            
            return result.toString();
        } catch (Exception e) {
            log.error("检索文档失败，查询: {}", query, e);
            return "检索文档失败: " + e.getMessage();
        }
    }
}

