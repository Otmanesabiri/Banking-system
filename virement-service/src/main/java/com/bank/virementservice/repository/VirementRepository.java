package com.bank.virementservice.repository;

import com.bank.virementservice.model.Virement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VirementRepository extends JpaRepository<Virement, Long> {
    List<Virement> findBySourceAccount(String sourceAccount);
    List<Virement> findByDestinationAccount(String destinationAccount);
    List<Virement> findByDateExecutionBetween(LocalDateTime start, LocalDateTime end);
}
