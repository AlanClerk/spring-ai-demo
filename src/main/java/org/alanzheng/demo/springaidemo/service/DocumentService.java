package org.alanzheng.demo.springaidemo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 文档服务类
 * 负责加载知识库文档并添加到向量存储
 */
@Slf4j
@Service
public class DocumentService {
    
    private static final String[] SUPPORTED_TEXT_EXTENSIONS = {".txt", ".md", ".text"};
    private static final String PDF_EXTENSION = ".pdf";
    
    private final VectorStore vectorStore;
    private final TextSplitter textSplitter;
    private final ResourceLoader resourceLoader;
    
    @Value("${spring.ai.rag.knowledge-base.path:./knowledge-base}")
    private String knowledgeBasePath;
    
    public DocumentService(VectorStore vectorStore, 
                          TextSplitter textSplitter,
                          ResourceLoader resourceLoader) {
        Objects.requireNonNull(vectorStore, "VectorStore不能为空");
        Objects.requireNonNull(textSplitter, "TextSplitter不能为空");
        Objects.requireNonNull(resourceLoader, "ResourceLoader不能为空");
        this.vectorStore = vectorStore;
        this.textSplitter = textSplitter;
        this.resourceLoader = resourceLoader;
    }
    
    /**
     * 加载知识库中的所有文档
     * 
     * @return 加载的文档数量
     */
    public int loadAllDocuments() {
        long startTime = System.currentTimeMillis();
        log.info("开始加载知识库文档，路径: {}", knowledgeBasePath);
        
        try {
            Path knowledgeBaseDir = Paths.get(knowledgeBasePath);
            if (!Files.exists(knowledgeBaseDir)) {
                log.warn("知识库目录不存在，将创建: {}", knowledgeBasePath);
                Files.createDirectories(knowledgeBaseDir);
                return 0;
            }
            
            List<Document> allDocuments = new ArrayList<>();
            loadDocumentsFromDirectory(knowledgeBaseDir, allDocuments);
            
            if (allDocuments.isEmpty()) {
                log.warn("知识库目录中没有找到可加载的文档");
                return 0;
            }
            
            log.info("读取到 {} 个文档，准备直接上传（不分割）", allDocuments.size());
            
            // 直接添加到向量存储（不分割）
            vectorStore.add(allDocuments);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("知识库文档加载完成，耗时: {}ms，加载文档数: {}", 
                    duration, allDocuments.size());
            
            return allDocuments.size();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("加载知识库文档失败，耗时: {}ms，错误信息: {}", duration, e.getMessage(), e);
            throw new RuntimeException("加载知识库文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 加载指定文件到向量存储
     * 
     * @param filePath 文件路径
     * @return 加载的文档块数量
     */
    public int loadDocument(String filePath) {
        long startTime = System.currentTimeMillis();
        log.info("开始加载单个文档，路径: {}", filePath);
        
        try {
            if (StringUtils.isBlank(filePath)) {
                throw new IllegalArgumentException("文件路径不能为空");
            }
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("文件不存在: " + filePath);
            }
            
            List<Document> documents = loadDocumentFromFile(path);
            if (documents.isEmpty()) {
                log.warn("文件加载后为空: {}", filePath);
                return 0;
            }
            
            log.info("读取到 {} 个文档，准备直接上传（不分割）", documents.size());
            
            // 直接添加到向量存储（不分割）
            vectorStore.add(documents);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("单个文档加载完成，耗时: {}ms，文档数: {}", duration, documents.size());
            
            return documents.size();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("加载单个文档失败，耗时: {}ms，路径: {}，错误信息: {}", 
                    duration, filePath, e.getMessage(), e);
            throw new RuntimeException("加载文档失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 递归加载目录中的所有文档
     */
    private void loadDocumentsFromDirectory(Path directory, List<Document> documents) throws IOException {
        if (!Files.isDirectory(directory)) {
            return;
        }
        
        Files.list(directory).forEach(path -> {
            try {
                if (Files.isDirectory(path)) {
                    // 递归处理子目录
                    loadDocumentsFromDirectory(path, documents);
                } else if (Files.isRegularFile(path)) {
                    // 处理文件
                    List<Document> fileDocuments = loadDocumentFromFile(path);
                    documents.addAll(fileDocuments);
                }
            } catch (Exception e) {
                log.warn("加载文件失败: {}，错误: {}", path, e.getMessage());
            }
        });
    }
    
    /**
     * 从文件加载文档
     */
    private List<Document> loadDocumentFromFile(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString().toLowerCase();
        
        if (fileName.endsWith(PDF_EXTENSION)) {
            return loadPdfDocument(filePath);
        } else if (isTextFile(fileName)) {
            return loadTextDocument(filePath);
        } else {
            // 尝试使用Tika自动检测文件类型
            return loadDocumentWithTika(filePath);
        }
    }
    
    /**
     * 加载PDF文档
     */
    private List<Document> loadPdfDocument(Path filePath) {
        try {
            Resource resource = resourceLoader.getResource("file:" + filePath.toAbsolutePath());
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                    resource, 
                    PdfDocumentReaderConfig.defaultConfig());
            return pdfReader.get();
        } catch (Exception e) {
            log.error("加载PDF文档失败: {}，错误: {}", filePath, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 加载文本文档
     */
    private List<Document> loadTextDocument(Path filePath) throws IOException {
        try {
            String content = Files.readString(filePath);
            if (StringUtils.isBlank(content)) {
                return Collections.emptyList();
            }
            
            Document document = new Document(content);
            document.getMetadata().put("source", filePath.toString());
            document.getMetadata().put("filename", filePath.getFileName().toString());
            
            return Collections.singletonList(document);
        } catch (Exception e) {
            log.error("加载文本文档失败: {}，错误: {}", filePath, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 使用Tika加载文档（自动检测文件类型）
     */
    private List<Document> loadDocumentWithTika(Path filePath) {
        try {
            Resource resource = resourceLoader.getResource("file:" + filePath.toAbsolutePath());
            TikaDocumentReader tikaReader = new TikaDocumentReader(resource);
            return tikaReader.get();
        } catch (Exception e) {
            log.warn("使用Tika加载文档失败: {}，错误: {}", filePath, e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * 判断是否为文本文件
     */
    private boolean isTextFile(String fileName) {
        for (String ext : SUPPORTED_TEXT_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 清空向量存储
     */
    public void clearVectorStore() {
        log.info("清空向量存储");
        try {
            // SimpleVectorStore没有直接的清空方法，需要重新创建
            // 这里记录日志，实际清空操作可能需要重新初始化VectorStore
            log.warn("清空向量存储功能需要重新初始化VectorStore，当前仅记录操作");
        } catch (Exception e) {
            log.error("清空向量存储失败，错误信息: {}", e.getMessage(), e);
            throw new RuntimeException("清空向量存储失败: " + e.getMessage(), e);
        }
    }
}

