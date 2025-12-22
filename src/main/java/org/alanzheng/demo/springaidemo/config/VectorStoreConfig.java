package org.alanzheng.demo.springaidemo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * 向量存储配置类
 * 配置嵌入模型和向量存储
 */
@Slf4j
@Configuration
public class VectorStoreConfig {
    
    @Value("${spring.ai.rag.vector-store.path:./vector-store}")
    private String vectorStorePath;
    
    @Value("${spring.ai.rag.chunk-size:1000}")
    private int chunkSize;
    
    @Value("${spring.ai.rag.chunk-overlap:200}")
    private int chunkOverlap;
    
    /**
     * 配置文本分割器
     * 用于将长文档分割成较小的块，便于向量化和检索
     * 
     * @return 文本分割器
     */
    @Bean
    public TextSplitter textSplitter() {
        log.info("初始化文本分割器，块大小: {}，重叠大小: {}", chunkSize, chunkOverlap);
        // TokenTextSplitter构造函数参数：chunkSize, chunkOverlap, maxChunkSize, minChunkSize, keepSeparator
        return new TokenTextSplitter(chunkSize, chunkOverlap, chunkSize * 2, chunkSize / 2, false);
    }
    
    /**
     * 配置向量存储
     * 使用SimpleVectorStore作为内存向量存储
     * 
     * @param embeddingModel 嵌入模型
     * @return 向量存储
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        Objects.requireNonNull(embeddingModel, "EmbeddingModel不能为空");
        
        log.info("初始化向量存储（内存模式）");
        
        // 创建SimpleVectorStore实例
        // 注意：SimpleVectorStore是内存存储，应用重启后数据会丢失
        // 如需持久化，可以考虑使用其他向量存储实现（如PGVector等）
        VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        
        log.info("向量存储初始化完成（内存模式）");
        
        return vectorStore;
    }
}

