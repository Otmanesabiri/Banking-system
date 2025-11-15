package com.bank.chatbotservice.service.rag;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * Service for extracting document content using Azure AI Document Intelligence
 * This service provides advanced document analysis capabilities including:
 * - Text extraction with layout preservation
 * - Table extraction and structuring
 * - Key-value pair extraction
 * - Document structure analysis
 */
@Service
@Slf4j
public class AzureDocumentIntelligenceService {
    
    private final DocumentAnalysisClient client;
    private final boolean enabled;
    
    public AzureDocumentIntelligenceService(
            @Value("${azure.document-intelligence.endpoint:}") String endpoint,
            @Value("${azure.document-intelligence.key:}") String key,
            @Value("${azure.document-intelligence.enabled:false}") boolean enabled) {
        
        this.enabled = enabled;
        
        if (enabled && endpoint != null && !endpoint.isEmpty() && key != null && !key.isEmpty()) {
            this.client = new DocumentAnalysisClientBuilder()
                    .credential(new AzureKeyCredential(key))
                    .endpoint(endpoint)
                    .buildClient();
            log.info("Azure Document Intelligence service initialized successfully");
        } else {
            this.client = null;
            log.info("Azure Document Intelligence service disabled or not configured");
        }
    }
    
    /**
     * Check if Azure Document Intelligence is enabled and configured
     */
    public boolean isEnabled() {
        return enabled && client != null;
    }
    
    /**
     * Extract text and structure from document using Azure AI
     */
    public List<Document> extractDocumentContent(Resource resource) {
        if (!isEnabled()) {
            throw new IllegalStateException("Azure Document Intelligence is not enabled or configured");
        }
        
        try {
            log.info("Analyzing document with Azure Document Intelligence: {}", resource.getFilename());
            
            // Convert Resource to BinaryData
            BinaryData documentData = BinaryData.fromBytes(resource.getContentAsByteArray());
            
            // Start the document analysis
            SyncPoller<OperationResult, AnalyzeResult> analyzePoller = 
                client.beginAnalyzeDocument("prebuilt-layout", documentData);
            
            // Wait for completion and get result
            AnalyzeResult analyzeResult = analyzePoller.getFinalResult();
            
            log.info("Document analysis completed. Found {} pages", analyzeResult.getPages().size());
            
            return convertAnalyzeResultToDocuments(analyzeResult, resource.getFilename());
            
        } catch (Exception e) {
            log.error("Error analyzing document with Azure Document Intelligence", e);
            throw new RuntimeException("Failed to analyze document with Azure AI", e);
        }
    }
    
    /**
     * Convert Azure AnalyzeResult to Spring AI Documents
     * Each page becomes a separate document for better granularity
     */
    private List<Document> convertAnalyzeResultToDocuments(AnalyzeResult result, String filename) {
        List<Document> documents = new ArrayList<>();
        
        // Process each page
        for (DocumentPage page : result.getPages()) {
            StringBuilder pageContent = new StringBuilder();
            Map<String, Object> metadata = new HashMap<>();
            
            // Page metadata
            int pageNumber = page.getPageNumber();
            metadata.put("page_number", pageNumber);
            metadata.put("filename", filename);
            metadata.put("width", page.getWidth());
            metadata.put("height", page.getHeight());
            metadata.put("unit", page.getUnit() != null ? page.getUnit().toString() : "");
            
            // Extract text lines with layout information
            if (page.getLines() != null) {
                for (DocumentLine line : page.getLines()) {
                    pageContent.append(line.getContent()).append("\n");
                }
            }
            
            // Extract tables from this page
            List<String> pageTables = extractTablesForPage(result, pageNumber);
            if (!pageTables.isEmpty()) {
                pageContent.append("\n=== TABLEAUX ===\n");
                for (String table : pageTables) {
                    pageContent.append(table).append("\n\n");
                }
                metadata.put("has_tables", true);
                metadata.put("table_count", pageTables.size());
            }
            
            // Create document for this page
            Document document = new Document(
                    UUID.randomUUID().toString(),
                    pageContent.toString(),
                    metadata
            );
            
            documents.add(document);
            log.debug("Processed page {} with {} characters", pageNumber, pageContent.length());
        }
        
        // Extract key-value pairs (for invoices, forms, etc.)
        List<String> keyValuePairs = extractKeyValuePairs(result);
        if (!keyValuePairs.isEmpty()) {
            Map<String, Object> kvMetadata = new HashMap<>();
            kvMetadata.put("filename", filename);
            kvMetadata.put("type", "key_value_pairs");
            
            StringBuilder kvContent = new StringBuilder("=== INFORMATIONS EXTRAITES ===\n");
            for (String pair : keyValuePairs) {
                kvContent.append(pair).append("\n");
            }
            
            Document kvDocument = new Document(
                    UUID.randomUUID().toString(),
                    kvContent.toString(),
                    kvMetadata
            );
            documents.add(kvDocument);
        }
        
        log.info("Converted analysis result to {} documents", documents.size());
        return documents;
    }
    
    /**
     * Extract tables for a specific page
     */
    private List<String> extractTablesForPage(AnalyzeResult result, int pageNumber) {
        List<String> tables = new ArrayList<>();
        
        if (result.getTables() != null) {
            for (DocumentTable table : result.getTables()) {
                // Check if table is on the specified page
                if (isTableOnPage(table, pageNumber)) {
                    String tableText = formatTable(table);
                    tables.add(tableText);
                }
            }
        }
        
        return tables;
    }
    
    /**
     * Check if table is on the specified page
     */
    private boolean isTableOnPage(DocumentTable table, int pageNumber) {
        if (table.getBoundingRegions() != null && !table.getBoundingRegions().isEmpty()) {
            return table.getBoundingRegions().stream()
                    .anyMatch(region -> region.getPageNumber() == pageNumber);
        }
        return false;
    }
    
    /**
     * Format table as text (Markdown-style)
     */
    private String formatTable(DocumentTable table) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tableau (").append(table.getRowCount()).append(" lignes, ")
          .append(table.getColumnCount()).append(" colonnes):\n");
        
        // Create a grid to hold cell contents
        String[][] grid = new String[table.getRowCount()][table.getColumnCount()];
        for (int i = 0; i < table.getRowCount(); i++) {
            for (int j = 0; j < table.getColumnCount(); j++) {
                grid[i][j] = "";
            }
        }
        
        // Fill grid with cell contents
        for (DocumentTableCell cell : table.getCells()) {
            int row = cell.getRowIndex();
            int col = cell.getColumnIndex();
            if (row < grid.length && col < grid[0].length) {
                grid[row][col] = cell.getContent();
            }
        }
        
        // Format as table
        for (int i = 0; i < grid.length; i++) {
            sb.append("| ");
            for (int j = 0; j < grid[i].length; j++) {
                sb.append(grid[i][j]).append(" | ");
            }
            sb.append("\n");
            
            // Add separator after header row
            if (i == 0) {
                sb.append("|");
                for (int j = 0; j < grid[i].length; j++) {
                    sb.append("---|");
                }
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Extract key-value pairs from document (useful for invoices, forms)
     */
    private List<String> extractKeyValuePairs(AnalyzeResult result) {
        List<String> pairs = new ArrayList<>();
        
        if (result.getKeyValuePairs() != null) {
            for (DocumentKeyValuePair kvPair : result.getKeyValuePairs()) {
                String key = kvPair.getKey() != null ? kvPair.getKey().getContent() : "";
                String value = kvPair.getValue() != null ? kvPair.getValue().getContent() : "";
                
                if (!key.isEmpty()) {
                    pairs.add(key + ": " + value);
                }
            }
        }
        
        return pairs;
    }
    
    /**
     * Analyze a specific document type (invoice, receipt, etc.)
     */
    public AnalyzeResult analyzeDocumentWithModel(Resource resource, String modelId) {
        if (!isEnabled()) {
            throw new IllegalStateException("Azure Document Intelligence is not enabled or configured");
        }
        
        try {
            log.info("Analyzing document with model '{}': {}", modelId, resource.getFilename());
            
            // Convert Resource to BinaryData
            BinaryData documentData = BinaryData.fromBytes(resource.getContentAsByteArray());
            
            SyncPoller<OperationResult, AnalyzeResult> analyzePoller = 
                client.beginAnalyzeDocument(modelId, documentData);
            
            return analyzePoller.getFinalResult();
            
        } catch (Exception e) {
            log.error("Error analyzing document with model: {}", modelId, e);
            throw new RuntimeException("Failed to analyze document with Azure AI", e);
        }
    }
    
    /**
     * Extract structured data from invoices
     */
    public Map<String, Object> extractInvoiceData(Resource resource) {
        AnalyzeResult result = analyzeDocumentWithModel(resource, "prebuilt-invoice");
        Map<String, Object> invoiceData = new HashMap<>();
        
        if (result.getDocuments() != null && !result.getDocuments().isEmpty()) {
            AnalyzedDocument document = result.getDocuments().get(0);
            
            // Extract common invoice fields
            extractField(document, "InvoiceId", invoiceData);
            extractField(document, "InvoiceDate", invoiceData);
            extractField(document, "DueDate", invoiceData);
            extractField(document, "VendorName", invoiceData);
            extractField(document, "CustomerName", invoiceData);
            extractField(document, "InvoiceTotal", invoiceData);
            extractField(document, "AmountDue", invoiceData);
        }
        
        return invoiceData;
    }
    
    private void extractField(AnalyzedDocument document, String fieldName, Map<String, Object> target) {
        if (document.getFields() != null && document.getFields().containsKey(fieldName)) {
            DocumentField field = document.getFields().get(fieldName);
            if (field != null && field.getContent() != null) {
                target.put(fieldName, field.getContent());
            }
        }
    }
}
