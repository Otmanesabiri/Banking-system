package com.bank.virementservice.dto;

import com.bank.virementservice.model.TypeVirement;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VirementDTO {
    private Long id;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal montant;
    private TypeVirement type;
    private LocalDateTime dateExecution;
    private String statut;
    private String motif;
}
