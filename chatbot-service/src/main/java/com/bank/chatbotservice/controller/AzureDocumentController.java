package com.bank.chatbotservice.controller;

import com.bank.chatbotservice.service.rag.AzureDocumentIntelligenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for Azure Document Intelligence operations
 */
@RestController
@RequestMapping("/api/azure-document")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Azure Document Intelligence", description = "API pour l'analyse de documents avec Azure AI")
public class AzureDocumentController {
    
    private final AzureDocumentIntelligenceService azureDocumentService;
    
    /**
     * Check if Azure Document Intelligence is enabled
     */
    @GetMapping("/status")
    @Operation(summary = "Vérifier le statut d'Azure Document Intelligence")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", azureDocumentService.isEnabled());
        status.put("service", "Azure AI Document Intelligence");
        status.put("capabilities", List.of(
            "Text extraction",
            "Table extraction",
            "Key-value pair extraction",
            "Layout analysis",
            "Invoice analysis",
            "Receipt analysis"
        ));
        return ResponseEntity.ok(status);
    }
    
    /**
     * Analyze document with Azure AI (general layout)
     */
    @PostMapping("/analyze")
    @Operation(
        summary = "Analyser un document avec Azure AI",
        description = "Extrait le texte, les tableaux et la structure d'un document"
    )
    public ResponseEntity<Map<String, Object>> analyzeDocument(
            @Parameter(description = "Fichier à analyser (PDF, image, etc.)")
            @RequestParam("file") MultipartFile file) {
        
        if (!azureDocumentService.isEnabled()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Azure Document Intelligence is not enabled",
                "message", "Please configure azure.document-intelligence.enabled=true"
            ));
        }
        
        try {
            log.info("Analyzing document: {}", file.getOriginalFilename());
            
            // Convert MultipartFile to Resource
            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            
            // Extract content
            List<Document> documents = azureDocumentService.extractDocumentContent(resource);
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", file.getOriginalFilename());
            response.put("pages_analyzed", documents.size());
            response.put("documents", documents.stream()
                .map(doc -> Map.of(
                    "id", doc.getId(),
                    "content", doc.getContent(),
                    "metadata", doc.getMetadata()
                ))
                .toList()
            );
            
            log.info("Successfully analyzed document: {} pages", documents.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error analyzing document", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Analyze invoice with Azure AI
     */
    @PostMapping("/analyze-invoice")
    @Operation(
        summary = "Analyser une facture avec Azure AI",
        description = "Extrait les champs structurés d'une facture (montant, date, vendeur, etc.)"
    )
    public ResponseEntity<Map<String, Object>> analyzeInvoice(
            @Parameter(description = "Fichier facture à analyser")
            @RequestParam("file") MultipartFile file) {
        
        if (!azureDocumentService.isEnabled()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Azure Document Intelligence is not enabled"
            ));
        }
        
        try {
            log.info("Analyzing invoice: {}", file.getOriginalFilename());
            
            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            
            // Extract invoice data
            Map<String, Object> invoiceData = azureDocumentService.extractInvoiceData(resource);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", file.getOriginalFilename());
            response.put("invoice_data", invoiceData);
            
            log.info("Successfully analyzed invoice");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error analyzing invoice", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Preview extracted text from document
     */
    @PostMapping("/preview")
    @Operation(
        summary = "Prévisualiser le texte extrait",
        description = "Extrait et affiche le texte brut d'un document"
    )
    public ResponseEntity<Map<String, Object>> previewDocument(
            @RequestParam("file") MultipartFile file) {
        
        if (!azureDocumentService.isEnabled()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Azure Document Intelligence is not enabled"
            ));
        }
        
        try {
            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            
            List<Document> documents = azureDocumentService.extractDocumentContent(resource);
            
            // Combine all text
            StringBuilder fullText = new StringBuilder();
            for (Document doc : documents) {
                fullText.append(doc.getContent()).append("\n\n");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", file.getOriginalFilename());
            response.put("text", fullText.toString());
            response.put("page_count", documents.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error previewing document", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
