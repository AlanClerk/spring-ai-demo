package org.alanzheng.demo.springaidemo.controller;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.alanzheng.demo.springaidemo.mcp.McpTools;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * MCP 控制器
 * 提供接口展示和测试 MCP 功能
 */
@Slf4j
@RestController
@RequestMapping("/api/mcp")
public class McpController {
    
    private final McpTools mcpTools;
    
    @Value("${spring.ai.mcp.server.enabled:false}")
    private boolean mcpServerEnabled;
    
    @Value("${spring.ai.mcp.server.port:8081}")
    private int mcpServerPort;
    
    @Value("${spring.ai.mcp.server.path:/mcp}")
    private String mcpServerPath;
    
    @Value("${spring.ai.mcp.server.name:spring-ai-demo-mcp-server}")
    private String mcpServerName;
    
    @Value("${spring.ai.mcp.server.version:1.0.0}")
    private String mcpServerVersion;
    
    public McpController(McpTools mcpTools) {
        this.mcpTools = mcpTools;
    }
    
    /**
     * 获取 MCP Server 信息
     * 
     * @return MCP Server 配置信息
     */
    @GetMapping("/info")
    public ResponseEntity<McpServerInfo> getServerInfo() {
        log.info("收到获取MCP Server信息请求");
        
        try {
            McpServerInfo info = McpServerInfo.builder()
                    .enabled(mcpServerEnabled)
                    .name(mcpServerName)
                    .version(mcpServerVersion)
                    .port(mcpServerPort)
                    .path(mcpServerPath)
                    .baseUrl(String.format("http://localhost:%d%s", mcpServerPort, mcpServerPath))
                    .build();
            
            log.info("返回MCP Server信息: {}", info);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("获取MCP Server信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取所有可用的 MCP 工具列表
     * 
     * @return 工具列表
     */
    @GetMapping("/tools")
    public ResponseEntity<McpToolsResponse> getTools() {
        log.info("收到获取MCP工具列表请求");
        
        try {
            List<ToolInfo> tools = extractToolInfo();
            
            McpToolsResponse response = McpToolsResponse.builder()
                    .success(true)
                    .toolCount(tools.size())
                    .tools(tools)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            log.info("返回MCP工具列表，工具数量: {}", tools.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取MCP工具列表失败", e);
            
            McpToolsResponse errorResponse = McpToolsResponse.builder()
                    .success(false)
                    .toolCount(0)
                    .tools(Collections.emptyList())
                    .errorMessage("获取工具列表失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 测试调用 MCP 工具：加载所有文档
     * 
     * @return 执行结果
     */
    @PostMapping("/tools/test/load-all-documents")
    public ResponseEntity<McpToolResult> testLoadAllDocuments() {
        log.info("收到测试调用MCP工具请求：loadAllDocuments");
        
        try {
            long startTime = System.currentTimeMillis();
            String result = mcpTools.loadAllDocuments();
            long duration = System.currentTimeMillis() - startTime;
            
            McpToolResult toolResult = McpToolResult.builder()
                    .toolName("loadAllDocuments")
                    .success(true)
                    .result(result)
                    .duration(duration)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            log.info("测试调用MCP工具成功，耗时: {}ms", duration);
            return ResponseEntity.ok(toolResult);
        } catch (Exception e) {
            log.error("测试调用MCP工具失败", e);
            
            McpToolResult errorResult = McpToolResult.builder()
                    .toolName("loadAllDocuments")
                    .success(false)
                    .result("调用失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * 测试调用 MCP 工具：加载指定文档
     * 
     * @param filePath 文件路径
     * @return 执行结果
     */
    @PostMapping("/tools/test/load-document")
    public ResponseEntity<McpToolResult> testLoadDocument(@RequestParam String filePath) {
        log.info("收到测试调用MCP工具请求：loadDocument，文件路径: {}", filePath);
        
        if (StringUtils.isBlank(filePath)) {
            McpToolResult errorResult = McpToolResult.builder()
                    .toolName("loadDocument")
                    .success(false)
                    .result("错误：文件路径不能为空")
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.badRequest().body(errorResult);
        }
        
        try {
            long startTime = System.currentTimeMillis();
            String result = mcpTools.loadDocument(filePath);
            long duration = System.currentTimeMillis() - startTime;
            
            McpToolResult toolResult = McpToolResult.builder()
                    .toolName("loadDocument")
                    .success(true)
                    .result(result)
                    .duration(duration)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            log.info("测试调用MCP工具成功，耗时: {}ms", duration);
            return ResponseEntity.ok(toolResult);
        } catch (Exception e) {
            log.error("测试调用MCP工具失败", e);
            
            McpToolResult errorResult = McpToolResult.builder()
                    .toolName("loadDocument")
                    .success(false)
                    .result("调用失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * 测试调用 MCP 工具：回答问题
     * 
     * @param question 用户问题
     * @return 执行结果
     */
    @PostMapping("/tools/test/answer-question")
    public ResponseEntity<McpToolResult> testAnswerQuestion(@RequestParam String question) {
        log.info("收到测试调用MCP工具请求：answerQuestion，问题: {}", question);
        
        if (StringUtils.isBlank(question)) {
            McpToolResult errorResult = McpToolResult.builder()
                    .toolName("answerQuestion")
                    .success(false)
                    .result("错误：问题不能为空")
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.badRequest().body(errorResult);
        }
        
        try {
            long startTime = System.currentTimeMillis();
            String result = mcpTools.answerQuestion(question);
            long duration = System.currentTimeMillis() - startTime;
            
            McpToolResult toolResult = McpToolResult.builder()
                    .toolName("answerQuestion")
                    .success(true)
                    .result(result)
                    .duration(duration)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            log.info("测试调用MCP工具成功，耗时: {}ms", duration);
            return ResponseEntity.ok(toolResult);
        } catch (Exception e) {
            log.error("测试调用MCP工具失败", e);
            
            McpToolResult errorResult = McpToolResult.builder()
                    .toolName("answerQuestion")
                    .success(false)
                    .result("调用失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * 测试调用 MCP 工具：检索文档
     * 
     * @param query 查询文本
     * @param topK 返回的文档数量（可选）
     * @return 执行结果
     */
    @PostMapping("/tools/test/search-documents")
    public ResponseEntity<McpToolResult> testSearchDocuments(
            @RequestParam String query,
            @RequestParam(required = false) Integer topK) {
        
        log.info("收到测试调用MCP工具请求：searchDocuments，查询: {}，topK: {}", query, topK);
        
        if (StringUtils.isBlank(query)) {
            McpToolResult errorResult = McpToolResult.builder()
                    .toolName("searchDocuments")
                    .success(false)
                    .result("错误：查询文本不能为空")
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.badRequest().body(errorResult);
        }
        
        try {
            long startTime = System.currentTimeMillis();
            String result;
            if (Objects.nonNull(topK)) {
                result = mcpTools.searchDocuments(query, topK);
            } else {
                result = mcpTools.searchDocuments(query);
            }
            long duration = System.currentTimeMillis() - startTime;
            
            McpToolResult toolResult = McpToolResult.builder()
                    .toolName("searchDocuments")
                    .success(true)
                    .result(result)
                    .duration(duration)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            log.info("测试调用MCP工具成功，耗时: {}ms", duration);
            return ResponseEntity.ok(toolResult);
        } catch (Exception e) {
            log.error("测试调用MCP工具失败", e);
            
            McpToolResult errorResult = McpToolResult.builder()
                    .toolName("searchDocuments")
                    .success(false)
                    .result("调用失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * 从 McpTools 类中提取工具信息
     * 
     * @return 工具信息列表
     */
    private List<ToolInfo> extractToolInfo() {
        List<ToolInfo> tools = new ArrayList<>();
        
        // loadAllDocuments 工具
        tools.add(ToolInfo.builder()
                .name("loadAllDocuments")
                .description("加载知识库中的所有文档到向量存储。返回加载的文档数量。")
                .parameters(Collections.emptyList())
                .returnType("String")
                .build());
        
        // loadDocument 工具
        tools.add(ToolInfo.builder()
                .name("loadDocument")
                .description("加载指定文件到向量存储。参数：filePath - 文件的完整路径。返回加载的文档数量。")
                .parameters(Collections.singletonList(
                        ParameterInfo.builder()
                                .name("filePath")
                                .type("String")
                                .required(true)
                                .build()))
                .returnType("String")
                .build());
        
        // answerQuestion 工具
        tools.add(ToolInfo.builder()
                .name("answerQuestion")
                .description("基于知识库回答用户问题。使用RAG（检索增强生成）技术，从知识库中检索相关信息并生成回答。参数：question - 用户的问题。")
                .parameters(Collections.singletonList(
                        ParameterInfo.builder()
                                .name("question")
                                .type("String")
                                .required(true)
                                .build()))
                .returnType("String")
                .build());
        
        // searchDocuments 工具（带topK参数）
        tools.add(ToolInfo.builder()
                .name("searchDocuments")
                .description("从知识库中检索与查询相关的文档。参数：query - 查询文本；topK - 返回的文档数量（可选，默认4）。返回检索到的文档摘要。")
                .parameters(Arrays.asList(
                        ParameterInfo.builder()
                                .name("query")
                                .type("String")
                                .required(true)
                                .build(),
                        ParameterInfo.builder()
                                .name("topK")
                                .type("Integer")
                                .required(false)
                                .build()))
                .returnType("String")
                .build());
        
        // searchDocuments 工具（简化版，不带topK参数）
        tools.add(ToolInfo.builder()
                .name("searchDocuments")
                .description("从知识库中检索与查询相关的文档（默认返回4个）。参数：query - 查询文本。")
                .parameters(Collections.singletonList(
                        ParameterInfo.builder()
                                .name("query")
                                .type("String")
                                .required(true)
                                .build()))
                .returnType("String")
                .build());
        
        return tools;
    }
    
    /**
     * MCP Server 信息
     */
    @Data
    @Builder
    public static class McpServerInfo {
        private boolean enabled;
        private String name;
        private String version;
        private int port;
        private String path;
        private String baseUrl;
    }
    
    /**
     * MCP 工具信息
     */
    @Data
    @Builder
    public static class ToolInfo {
        private String name;
        private String description;
        private List<ParameterInfo> parameters;
        private String returnType;
    }
    
    /**
     * 参数信息
     */
    @Data
    @Builder
    public static class ParameterInfo {
        private String name;
        private String type;
        private boolean required;
    }
    
    /**
     * MCP 工具列表响应
     */
    @Data
    @Builder
    public static class McpToolsResponse {
        private boolean success;
        private int toolCount;
        private List<ToolInfo> tools;
        private String errorMessage;
        private long timestamp;
    }
    
    /**
     * MCP 工具调用结果
     */
    @Data
    @Builder
    public static class McpToolResult {
        private String toolName;
        private boolean success;
        private String result;
        private Long duration;
        private long timestamp;
    }
}

