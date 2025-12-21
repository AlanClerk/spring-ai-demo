package org.alanzheng.demo.springaidemo.dto;

import lombok.Data;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {
    
    /**
     * 用户消息内容
     */
    private String message;
    
    /**
     * 对话ID（可选，用于维持上下文）
     */
    private String conversationId;
}

