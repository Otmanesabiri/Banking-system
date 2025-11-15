package com.bank.chatbotservice.repository;

import com.bank.chatbotservice.domain.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(String documentId);
    void deleteByDocumentId(String documentId);
}
