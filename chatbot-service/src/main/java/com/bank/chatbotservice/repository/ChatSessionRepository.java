package com.bank.chatbotservice.repository;

import com.bank.chatbotservice.domain.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findBySessionId(String sessionId);
    Optional<ChatSession> findBySessionIdAndIsActiveTrue(String sessionId);
}
