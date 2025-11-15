package com.bank.chatbotservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a chunk of a document for RAG retrieval
 */
@Entity
@Table(name = "document_chunk")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String documentId;
    
    @Column(nullable = false)
    private Integer chunkIndex;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "vector_id")
    private String vectorId;
    
    @Column(name = "token_count")
    private Integer tokenCount;
    
    @Column(name = "page_number")
    private Integer pageNumber;
    
    /**
     * Metadata as JSON string
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
}
