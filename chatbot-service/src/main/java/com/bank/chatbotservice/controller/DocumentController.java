package com.bank.chatbotservice.controller;

import com.bank.chatbotservice.domain.BankDocument;
import com.bank.chatbotservice.service.rag.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST API for document management
 */
@RestController
@RequestMapping("/api/documents")
@Slf4j
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentIngestionService ingestionService;
    
    /**
     * Ingest a new PDF document
     */
    @PostMapping("/ingest")
    public ResponseEntity<String> ingestDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam(value = "description", required = false) String description) {
        
        try {
            log.info("Ingesting document: {}", file.getOriginalFilename());
            
            if (!file.getOriginalFilename().endsWith(".pdf")) {
                return ResponseEntity.badRequest()
                        .body("Only PDF files are supported");
            }
            
            Resource resource = file.getResource();
            String documentId = ingestionService.ingestDocument(resource, category, description);
            
            return ResponseEntity.ok("Document ingested successfully. ID: " + documentId);
            
        } catch (Exception e) {
            log.error("Error ingesting document", e);
            return ResponseEntity.internalServerError()
                    .body("Error ingesting document: " + e.getMessage());
        }
    }
    
    /**
     * Get all documents
     */
    @GetMapping
    public ResponseEntity<List<BankDocument>> getAllDocuments() {
        List<BankDocument> documents = ingestionService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }
    
    /**
     * Get documents by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<BankDocument>> getDocumentsByCategory(@PathVariable String category) {
        List<BankDocument> documents = ingestionService.getDocumentsByCategory(category);
        return ResponseEntity.ok(documents);
    }
    
    /**
     * Delete a document
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        log.info("Deleting document: {}", documentId);
        ingestionService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
