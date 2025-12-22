package org.alanzheng.demo.springaidemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 文档信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInfo {
    
    /**
     * 文档内容
     */
    private String content;
    
    /**
     * 文档元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 文档来源
     */
    private String source;
}

