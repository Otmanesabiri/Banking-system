package com.bank.chatbotservice.service;

import com.bank.chatbotservice.config.ChatbotProperties;
import com.bank.chatbotservice.domain.ChatMessage;
import com.bank.chatbotservice.domain.ChatSession;
import com.bank.chatbotservice.dto.ChatRequest;
import com.bank.chatbotservice.dto.ChatResponse;
import com.bank.chatbotservice.repository.ChatSessionRepository;
import com.bank.chatbotservice.service.rag.DocumentRetrievalService;
import com.bank.chatbotservice.service.tools.BeneficiaireTools;
import com.bank.chatbotservice.service.tools.VirementTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main Chat Service integrating Spring AI, RAG, and MCP tools
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ChatSessionRepository sessionRepository;
    private final DocumentRetrievalService retrievalService;
    private final BeneficiaireTools beneficiaireTools;
    private final VirementTools virementTools;
    private final ChatbotProperties properties;
    
    /**
     * Process user message and generate response
     */
    @Transactional
    public ChatResponse processMessage(ChatRequest request) {
        try {
            // Get or create session
            ChatSession session = getOrCreateSession(request.getSessionId(), request.getUserId());
            
            // Retrieve relevant documents (RAG)
            List<String> relevantDocs = retrievalService.retrieveRelevantDocuments(request.getMessage());
            String context = retrievalService.buildContextFromDocuments(relevantDocs);
            
            // Build prompt with context
            String enhancedMessage = buildEnhancedPrompt(request.getMessage(), context);
            
            // Save user message
            ChatMessage userMessage = ChatMessage.builder()
                    .session(session)
                    .role(ChatMessage.MessageRole.USER)
                    .content(request.getMessage())
                    .hasImage(request.getImageUrl() != null)
                    .imageUrl(request.getImageUrl())
                    .build();
            session.addMessage(userMessage);
            
            // Create messages list
            List<Message> messages = new ArrayList<>();
            
            // System message
            messages.add(new SystemMessage(properties.getAi().getSystemPrompt()));
            
            // Add conversation history
            List<Message> history = chatMemory.get(session.getSessionId(), 10);
            messages.addAll(history);
            
            // User message (with image if provided)
            if (request.getImageUrl() != null) {
                messages.add(createMultimodalMessage(enhancedMessage, request.getImageUrl()));
            } else {
                messages.add(new UserMessage(enhancedMessage));
            }
            
            // Call LLM with tools
            String response = chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();
            
            // Save assistant message
            ChatMessage assistantMessage = ChatMessage.builder()
                    .session(session)
                    .role(ChatMessage.MessageRole.ASSISTANT)
                    .content(response)
                    .build();
            session.addMessage(assistantMessage);
            
            // Update memory
            chatMemory.add(session.getSessionId(), new UserMessage(request.getMessage()));
            chatMemory.add(session.getSessionId(), new AssistantMessage(response));
            
            // Save session
            sessionRepository.save(session);
            
            log.info("Generated response for session {}", session.getSessionId());
            
            return ChatResponse.builder()
                    .sessionId(session.getSessionId())
                    .message(response)
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing message", e);
            return ChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .message("Désolé, une erreur s'est produite.")
                    .timestamp(LocalDateTime.now())
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }
    
    /**
     * Process message with streaming response
     */
    @Transactional
    public void processMessageStreaming(ChatRequest request, StreamingResponseHandler handler) {
        try {
            final ChatSession session = getOrCreateSession(request.getSessionId(), request.getUserId());
            final String sessionId = session.getSessionId();
            
            // Retrieve relevant documents
            List<String> relevantDocs = retrievalService.retrieveRelevantDocuments(request.getMessage());
            String context = retrievalService.buildContextFromDocuments(relevantDocs);
            
            String enhancedMessage = buildEnhancedPrompt(request.getMessage(), context);
            
            // Build messages
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(properties.getAi().getSystemPrompt()));
            
            List<Message> history = chatMemory.get(sessionId, 10);
            messages.addAll(history);
            messages.add(new UserMessage(enhancedMessage));
            
            // Stream response
            StringBuilder fullResponse = new StringBuilder();
            
            chatClient.prompt()
                    .messages(messages)
                    .stream()
                    .content()
                    .subscribe(
                        chunk -> {
                            fullResponse.append(chunk);
                            handler.onChunk(chunk);
                        },
                        error -> {
                            log.error("Error in streaming", error);
                            handler.onError((Exception) error);
                        },
                        () -> {
                            // On complete - save messages
                            ChatMessage userMsg = ChatMessage.builder()
                                    .session(session)
                                    .role(ChatMessage.MessageRole.USER)
                                    .content(request.getMessage())
                                    .build();
                            
                            ChatMessage assistantMsg = ChatMessage.builder()
                                    .session(session)
                                    .role(ChatMessage.MessageRole.ASSISTANT)
                                    .content(fullResponse.toString())
                                    .build();
                            
                            session.addMessage(userMsg);
                            session.addMessage(assistantMsg);
                            sessionRepository.save(session);
                            
                            handler.onComplete();
                        }
                    );
            
        } catch (Exception e) {
            log.error("Error in streaming", e);
            handler.onError(e);
        }
    }
    
    /**
     * Get or create chat session
     */
    private ChatSession getOrCreateSession(String sessionId, String userId) {
        final String finalSessionId;
        if (sessionId == null || sessionId.isEmpty()) {
            finalSessionId = UUID.randomUUID().toString();
        } else {
            finalSessionId = sessionId;
        }
        
        return sessionRepository.findBySessionIdAndIsActiveTrue(finalSessionId)
                .orElseGet(() -> {
                    ChatSession newSession = ChatSession.builder()
                            .sessionId(finalSessionId)
                            .userId(userId != null ? userId : "anonymous")
                            .isActive(true)
                            .build();
                    return sessionRepository.save(newSession);
                });
    }
    
    /**
     * Build enhanced prompt with RAG context
     */
    private String buildEnhancedPrompt(String userMessage, String context) {
        return String.format("""
                Contexte provenant des documents:
                %s
                
                Question de l'utilisateur:
                %s
                
                Répondez en utilisant uniquement les informations du contexte fourni.
                Si l'information n'est pas disponible, dites "Je ne sais pas".
                """, context, userMessage);
    }
    
    /**
     * Create multimodal message with image
     */
    private UserMessage createMultimodalMessage(String text, String imageUrl) {
        try {
            Media media = new Media(MimeTypeUtils.IMAGE_JPEG, new URL(imageUrl));
            return new UserMessage(text, List.of(media));
        } catch (Exception e) {
            log.error("Error creating multimodal message", e);
            return new UserMessage(text);
        }
    }
    
    /**
     * Get session history
     */
    public List<ChatMessage> getSessionHistory(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .map(ChatSession::getMessages)
                .orElse(List.of());
    }
    
    /**
     * Clear session
     */
    @Transactional
    public void clearSession(String sessionId) {
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setIsActive(false);
            sessionRepository.save(session);
            chatMemory.clear(sessionId);
        });
    }
    
    /**
     * Streaming response handler interface
     */
    public interface StreamingResponseHandler {
        void onChunk(String chunk);
        void onComplete();
        void onError(Exception e);
    }
}
