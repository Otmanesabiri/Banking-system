package com.bank.chatbotservice.service;

import com.bank.chatbotservice.config.ChatbotProperties;
import com.bank.chatbotservice.domain.BankDocument;
import com.bank.chatbotservice.repository.BankDocumentRepository;
import com.bank.chatbotservice.service.rag.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service to initialize and auto-ingest documents on startup
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentInitializationService {
    
    private final DocumentIngestionService ingestionService;
    private final BankDocumentRepository documentRepository;
    private final ChatbotProperties properties;
    
    /**
     * Auto-ingest documents on application startup if enabled
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDocuments() {
        if (!properties.getRag().getAutoIngest()) {
            log.info("Auto-ingestion disabled. Skipping document initialization.");
            return;
        }
        
        log.info("Starting document auto-ingestion...");
        
        try {
            // Check if documents directory exists
            String documentsPath = properties.getRag().getDocumentsPath();
            Path docsDir = Paths.get(documentsPath);
            
            if (!Files.exists(docsDir)) {
                log.info("Creating documents directory: {}", documentsPath);
                Files.createDirectories(docsDir);
                createSampleDocuments(docsDir);
            }
            
            // Ingest all PDF files from documents directory
            ingestDocumentsFromDirectory(documentsPath);
            
            // Log statistics
            List<BankDocument> allDocs = documentRepository.findAll();
            long processedCount = allDocs.stream().filter(BankDocument::getProcessed).count();
            
            log.info("Document initialization complete. Total: {}, Processed: {}", 
                    allDocs.size(), processedCount);
            
        } catch (Exception e) {
            log.error("Error during document initialization", e);
        }
    }
    
    /**
     * Ingest all PDF and Markdown files from directory
     */
    private void ingestDocumentsFromDirectory(String directoryPath) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            
            // Support both PDF and Markdown files
            String pdfPattern = "file:" + directoryPath + "/**/*.pdf";
            String mdPattern = "file:" + directoryPath + "/**/*.md";
            
            Resource[] pdfResources = resolver.getResources(pdfPattern);
            Resource[] mdResources = resolver.getResources(mdPattern);
            
            // Combine both arrays
            Resource[] resources = new Resource[pdfResources.length + mdResources.length];
            System.arraycopy(pdfResources, 0, resources, 0, pdfResources.length);
            System.arraycopy(mdResources, 0, resources, pdfResources.length, mdResources.length);
            
            log.info("Found {} files in directory: {} ({} PDF, {} Markdown)", 
                    resources.length, directoryPath, pdfResources.length, mdResources.length);
            
            for (Resource resource : resources) {
                try {
                    String filename = resource.getFilename();
                    
                    // Check if already ingested
                    if (isDocumentAlreadyIngested(filename)) {
                        log.debug("Document already ingested: {}", filename);
                        continue;
                    }
                    
                    // Determine category from directory structure
                    String category = extractCategory(resource.getFile());
                    String description = "Document bancaire: " + filename;
                    
                    log.info("Ingesting document: {} (category: {})", filename, category);
                    ingestionService.ingestDocument(resource, category, description);
                    
                } catch (Exception e) {
                    log.error("Error ingesting document: {}", resource.getFilename(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error reading documents directory", e);
        }
    }
    
    /**
     * Check if document already ingested
     */
    private boolean isDocumentAlreadyIngested(String filename) {
        return documentRepository.findAll().stream()
                .anyMatch(doc -> doc.getFilename().equals(filename));
    }
    
    /**
     * Extract category from file path
     */
    private String extractCategory(File file) {
        String path = file.getAbsolutePath();
        
        if (path.contains("virement")) {
            return "virements";
        } else if (path.contains("beneficiaire")) {
            return "beneficiaires";
        } else if (path.contains("compte")) {
            return "comptes";
        } else if (path.contains("carte")) {
            return "cartes";
        } else if (path.contains("credit")) {
            return "credits";
        } else {
            return "general";
        }
    }
    
    /**
     * Create sample markdown documents (convert to PDF in production)
     */
    private void createSampleDocuments(Path docsDir) {
        log.info("Sample documents should be placed manually in: {}", docsDir);
        log.info("Supported formats: PDF, Markdown (.md)");
        log.info("Recommended structure:");
        log.info("  documents/");
        log.info("    virements/");
        log.info("    beneficiaires/");
        log.info("    general/");
    }
    
    /**
     * Manual trigger for re-ingestion
     */
    public void reingestAllDocuments() {
        log.info("Starting manual re-ingestion of all documents...");
        
        // Delete all existing documents
        List<BankDocument> allDocs = documentRepository.findAll();
        for (BankDocument doc : allDocs) {
            ingestionService.deleteDocument(doc.getDocumentId());
        }
        
        // Re-ingest
        String documentsPath = properties.getRag().getDocumentsPath();
        ingestDocumentsFromDirectory(documentsPath);
        
        log.info("Manual re-ingestion complete");
    }
}
