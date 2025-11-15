package com.bank.chatbotservice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a single message in a conversation
 */
@Entity
@Table(name = "chat_message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageRole role;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "has_image")
    private Boolean hasImage;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (hasImage == null) {
            hasImage = false;
        }
    }
    
    public enum MessageRole {
        USER,
        ASSISTANT,
        SYSTEM
    }
}
