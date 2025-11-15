package com.bank.chatbotservice.controller;

import com.bank.chatbotservice.domain.ChatMessage;
import com.bank.chatbotservice.dto.ChatRequest;
import com.bank.chatbotservice.dto.ChatResponse;
import com.bank.chatbotservice.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

/**
 * REST API for chatbot interactions
 */
@RestController
@RequestMapping("/api/chatbot")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Chatbot", description = "API du chatbot intelligent avec Spring AI et RAG")
public class ChatController {
    
    private final ChatService chatService;
    
    /**
     * Send message to chatbot
     */
    @Operation(
        summary = "Envoyer un message au chatbot",
        description = "Envoie un message texte au chatbot et reçoit une réponse générée par l'IA avec contexte RAG"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Réponse générée avec succès",
            content = @Content(schema = @Schema(implementation = ChatResponse.class))),
        @ApiResponse(responseCode = "400", description = "Requête invalide"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        log.info("Received chat request from user: {}", request.getUserId());
        ChatResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Send message with streaming response
     */
    @Operation(
        summary = "Envoyer un message avec streaming",
        description = "Envoie un message et reçoit la réponse en streaming (Server-Sent Events)"
    )
    @PostMapping(value = "/message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStreaming(@RequestBody ChatRequest request) {
        log.info("Received streaming chat request from user: {}", request.getUserId());
        
        SseEmitter emitter = new SseEmitter(60000L); // 60 seconds timeout
        
        chatService.processMessageStreaming(request, new ChatService.StreamingResponseHandler() {
            @Override
            public void onChunk(String chunk) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(chunk));
                } catch (IOException e) {
                    log.error("Error sending chunk", e);
                    emitter.completeWithError(e);
                }
            }
            
            @Override
            public void onComplete() {
                emitter.complete();
            }
            
            @Override
            public void onError(Exception e) {
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }
    
    /**
     * Send image for analysis
     */
    @PostMapping("/image")
    public ResponseEntity<ChatResponse> analyzeImage(@RequestBody ChatRequest request) {
        log.info("Received image analysis request from user: {}", request.getUserId());
        
        if (request.getImageUrl() == null || request.getImageUrl().isEmpty()) {
            return ResponseEntity.badRequest().body(ChatResponse.builder()
                    .success(false)
                    .error("Image URL is required")
                    .build());
        }
        
        ChatResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get conversation history
     */
    @Operation(
        summary = "Récupérer l'historique de conversation",
        description = "Récupère tous les messages d'une session de chat"
    )
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(
            @Parameter(description = "ID de la session") @PathVariable String sessionId) {
        log.info("Fetching history for session: {}", sessionId);
        List<ChatMessage> history = chatService.getSessionHistory(sessionId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Clear conversation history
     */
    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Void> clearHistory(@PathVariable String sessionId) {
        log.info("Clearing history for session: {}", sessionId);
        chatService.clearSession(sessionId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot service is running");
    }
}
