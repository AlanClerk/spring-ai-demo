package org.alanzheng.demo.springaidemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RAG请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagRequest {
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * 检索的文档数量（topK）
     */
    private Integer topK;
    
    /**
     * 相似度阈值
     */
    private Double similarityThreshold;
}

