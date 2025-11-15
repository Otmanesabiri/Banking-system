package com.bank.chatbotservice.service.rag;

import com.bank.chatbotservice.config.ChatbotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for retrieving relevant documents using vector similarity search
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentRetrievalService {
    
    private final VectorStore vectorStore;
    private final ChatbotProperties properties;
    
    /**
     * Retrieve most relevant documents for a query
     * 
     * @param query User's question
     * @return List of relevant document contents
     */
    public List<String> retrieveRelevantDocuments(String query) {
        return retrieveRelevantDocuments(query, properties.getRag().getTopK());
    }
    
    /**
     * Retrieve most relevant documents for a query with custom top-k
     * 
     * @param query User's question
     * @param topK Number of documents to retrieve
     * @return List of relevant document contents
     */
    public List<String> retrieveRelevantDocuments(String query, int topK) {
        log.debug("Retrieving top {} documents for query: {}", topK, query);
        
        try {
            SearchRequest searchRequest = SearchRequest.query(query)
                    .withTopK(topK)
                    .withSimilarityThreshold(0.7); // Only include documents with >70% similarity
            
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            
            log.info("Found {} relevant documents", results.size());
            
            return results.stream()
                    .map(Document::getContent)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error retrieving documents", e);
            return List.of();
        }
    }
    
    /**
     * Retrieve documents by category
     */
    public List<String> retrieveDocumentsByCategory(String query, String category, int topK) {
        log.debug("Retrieving top {} documents for query: {} in category: {}", topK, query, category);
        
        try {
            SearchRequest searchRequest = SearchRequest.query(query)
                    .withTopK(topK)
                    .withSimilarityThreshold(0.7)
                    .withFilterExpression(String.format("category == '%s'", category));
            
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            
            log.info("Found {} relevant documents in category {}", results.size(), category);
            
            return results.stream()
                    .map(Document::getContent)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error retrieving documents by category", e);
            return List.of();
        }
    }
    
    /**
     * Build context string from retrieved documents
     */
    public String buildContextFromDocuments(List<String> documents) {
        if (documents.isEmpty()) {
            return "Aucun document pertinent trouvé.";
        }
        
        StringBuilder context = new StringBuilder("Contexte basé sur les documents de la banque:\n\n");
        
        for (int i = 0; i < documents.size(); i++) {
            context.append(String.format("Document %d:\n%s\n\n", i + 1, documents.get(i)));
        }
        
        return context.toString();
    }
}
