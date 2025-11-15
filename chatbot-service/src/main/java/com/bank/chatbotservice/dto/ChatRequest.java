package com.bank.chatbotservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String sessionId;
    private String userId;
    private String message;
    private String imageUrl;
    private Boolean streaming;
}
