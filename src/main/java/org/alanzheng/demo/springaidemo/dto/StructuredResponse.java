package org.alanzheng.demo.springaidemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 结构化输出响应DTO
 * 用于封装结构化输出的结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredResponse<T> {
    
    /**
     * 结构化数据
     */
    private T data;
    
    /**
     * 响应时间戳
     */
    private Long timestamp;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;
}

