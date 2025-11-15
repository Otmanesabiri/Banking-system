package com.bank.chatbotservice.service.rag;

import com.bank.chatbotservice.config.ChatbotProperties;
import com.bank.chatbotservice.domain.BankDocument;
import com.bank.chatbotservice.domain.DocumentChunk;
import com.bank.chatbotservice.repository.BankDocumentRepository;
import com.bank.chatbotservice.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for ingesting PDF documents into the RAG system
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentIngestionService {
    
    private final BankDocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final ChatbotProperties properties;
    private final AzureDocumentIntelligenceService azureDocumentService;
    
    /**
     * Ingest a PDF document into the RAG system
     */
    @Transactional
    public String ingestDocument(Resource resource, String category, String description) {
        try {
            log.info("Starting ingestion of document: {}", resource.getFilename());
            
            // Create document record
            String documentId = UUID.randomUUID().toString();
            BankDocument bankDocument = BankDocument.builder()
                    .documentId(documentId)
                    .filename(resource.getFilename())
                    .filePath(resource.getFile().getAbsolutePath())
                    .category(category)
                    .description(description)
                    .fileSize(resource.contentLength())
                    .processed(false)
                    .build();
            
            documentRepository.save(bankDocument);
            
            // Extract text based on file type
            List<Document> documents;
            String filename = resource.getFilename();
            if (filename != null && filename.toLowerCase().endsWith(".md")) {
                documents = extractTextFromMarkdown(resource);
                log.info("Extracted content from Markdown file");
            } else {
                documents = extractTextFromPdf(resource);
                log.info("Extracted {} pages from PDF", documents.size());
            }
            
            // Split into chunks
            TokenTextSplitter splitter = new TokenTextSplitter(
                    properties.getRag().getChunkSize(),
                    properties.getRag().getChunkOverlap(),
                    5,  // min chunk size
                    10000,  // max chunk size
                    true  // keep separator
            );
            
            List<Document> chunks = splitter.apply(documents);
            log.info("Split into {} chunks", chunks.size());
            
            // Add metadata
            for (int i = 0; i < chunks.size(); i++) {
                Document chunk = chunks.get(i);
                chunk.getMetadata().put("documentId", documentId);
                chunk.getMetadata().put("filename", resource.getFilename());
                chunk.getMetadata().put("category", category);
                chunk.getMetadata().put("chunkIndex", i);
            }
            
            // Store in vector database
            vectorStore.add(chunks);
            log.info("Stored {} chunks in vector database", chunks.size());
            
            // Save chunk metadata
            saveChunkMetadata(documentId, chunks);
            
            // Update document as processed
            bankDocument.setProcessed(true);
            bankDocument.setProcessedAt(LocalDateTime.now());
            bankDocument.setChunkCount(chunks.size());
            documentRepository.save(bankDocument);
            
            log.info("Successfully ingested document: {}", documentId);
            return documentId;
            
        } catch (Exception e) {
            log.error("Error ingesting document: {}", resource.getFilename(), e);
            throw new RuntimeException("Failed to ingest document", e);
        }
    }
    
    /**
     * Extract text from PDF using Spring AI or Azure Document Intelligence
     */
    private List<Document> extractTextFromPdf(Resource resource) {
        try {
            // Use Azure Document Intelligence if available (better quality extraction)
            if (azureDocumentService.isEnabled()) {
                log.info("Using Azure Document Intelligence for extraction");
                return azureDocumentService.extractDocumentContent(resource);
            } else {
                // Fallback to Spring AI PDF reader
                log.info("Using Spring AI PDF reader for extraction");
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource);
                return pdfReader.get();
            }
            
        } catch (Exception e) {
            log.error("Error extracting text from PDF", e);
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }
    
    /**
     * Extract text from Markdown file
     */
    private List<Document> extractTextFromMarkdown(Resource resource) {
        try {
            // Read entire markdown file as one document
            String content = new String(resource.getInputStream().readAllBytes());
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", resource.getFilename());
            metadata.put("file_type", "markdown");
            
            Document document = new Document(content, metadata);
            return List.of(document);
            
        } catch (Exception e) {
            log.error("Error extracting text from Markdown", e);
            throw new RuntimeException("Failed to extract text from Markdown", e);
        }
    }
    
    /**
     * Save chunk metadata to database
     */
    private void saveChunkMetadata(String documentId, List<Document> chunks) {
        List<DocumentChunk> chunkEntities = new ArrayList<>();
        
        for (int i = 0; i < chunks.size(); i++) {
            Document chunk = chunks.get(i);
            
            DocumentChunk chunkEntity = DocumentChunk.builder()
                    .documentId(documentId)
                    .chunkIndex(i)
                    .content(chunk.getContent())
                    .vectorId(chunk.getId())
                    .tokenCount(estimateTokenCount(chunk.getContent()))
                    .pageNumber((Integer) chunk.getMetadata().getOrDefault("page_number", null))
                    .metadata(convertMetadataToJson(chunk.getMetadata()))
                    .build();
            
            chunkEntities.add(chunkEntity);
        }
        
        chunkRepository.saveAll(chunkEntities);
    }
    
    /**
     * Estimate token count (rough approximation)
     */
    private int estimateTokenCount(String text) {
        return (int) (text.split("\\s+").length * 1.3);
    }
    
    /**
     * Convert metadata to JSON string
     */
    private String convertMetadataToJson(Map<String, Object> metadata) {
        try {
            StringBuilder json = new StringBuilder("{");
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue()).append("\",");
            }
            if (json.length() > 1) {
                json.setLength(json.length() - 1); // Remove trailing comma
            }
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * Delete document and its chunks
     */
    @Transactional
    public void deleteDocument(String documentId) {
        log.info("Deleting document: {}", documentId);
        
        // Get chunks to delete from vector store
        List<DocumentChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);
        List<String> vectorIds = chunks.stream()
                .map(DocumentChunk::getVectorId)
                .filter(Objects::nonNull)
                .toList();
        
        // Delete from vector store
        if (!vectorIds.isEmpty()) {
            vectorStore.delete(vectorIds);
        }
        
        // Delete chunks from database
        chunkRepository.deleteByDocumentId(documentId);
        
        // Delete document record
        documentRepository.findByDocumentId(documentId)
                .ifPresent(documentRepository::delete);
        
        log.info("Deleted document: {}", documentId);
    }
    
    /**
     * Get all documents
     */
    public List<BankDocument> getAllDocuments() {
        return documentRepository.findAll();
    }
    
    /**
     * Get documents by category
     */
    public List<BankDocument> getDocumentsByCategory(String category) {
        return documentRepository.findByCategory(category);
    }
}
