package org.alanzheng.demo.springaidemo.dto;

import lombok.Data;

/**
 * Agent 请求 DTO
 */
@Data
public class AgentRequest {
    
    /**
     * 用户消息内容
     */
    private String message;
    
    /**
     * 自定义系统提示词（可选）
     */
    private String systemPrompt;
    
    /**
     * 对话ID（可选，用于维持上下文）
     */
    private String conversationId;
}

