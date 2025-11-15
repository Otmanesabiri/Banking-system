package com.bank.chatbotservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a bank document (PDF) used for RAG
 */
@Entity
@Table(name = "bank_document")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String documentId;
    
    @Column(nullable = false)
    private String filename;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private String category;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
    
    @Column(name = "processed")
    private Boolean processed;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "chunk_count")
    private Integer chunkCount;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (processed == null) {
            processed = false;
        }
    }
}
