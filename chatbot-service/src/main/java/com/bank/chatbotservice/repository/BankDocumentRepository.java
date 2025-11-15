package com.bank.chatbotservice.repository;

import com.bank.chatbotservice.domain.BankDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankDocumentRepository extends JpaRepository<BankDocument, Long> {
    Optional<BankDocument> findByDocumentId(String documentId);
    List<BankDocument> findByProcessedFalse();
    List<BankDocument> findByCategory(String category);
}
