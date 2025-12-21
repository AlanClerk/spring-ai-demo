package org.alanzheng.demo.springaidemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    /**
     * AI回复内容
     */
    private String reply;
    
    /**
     * 对话ID
     */
    private String conversationId;
    
    /**
     * 响应时间戳
     */
    private Long timestamp;
}

