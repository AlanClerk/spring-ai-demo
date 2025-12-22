package org.alanzheng.demo.springaidemo.config;

import lombok.extern.slf4j.Slf4j;
import org.alanzheng.demo.springaidemo.mcp.McpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Objects;

/**
 * MCP 配置类
 * 注册 MCP 工具，供 AI Agent 调用
 */
@Slf4j
@Configuration
public class McpConfig {
    
    /**
     * 注册 MCP 工具回调提供者
     * 将 McpTools 中定义的工具方法暴露给 MCP Server
     * 
     * @param mcpTools MCP 工具类实例
     * @return 工具回调提供者
     */
    @Bean
    public ToolCallbackProvider toolCallbackProvider(@Lazy McpTools mcpTools) {
        Objects.requireNonNull(mcpTools, "McpTools不能为空");
        
        log.info("注册 MCP 工具回调提供者");
        
        return MethodToolCallbackProvider.builder()
                .toolObjects(mcpTools)
                .build();
    }
}

