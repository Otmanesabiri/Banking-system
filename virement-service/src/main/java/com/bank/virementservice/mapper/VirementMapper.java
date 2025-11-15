package com.bank.virementservice.mapper;

import com.bank.virementservice.dto.VirementDTO;
import com.bank.virementservice.dto.VirementRequest;
import com.bank.virementservice.dto.VirementResponse;
import com.bank.virementservice.model.Virement;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VirementMapper {

    public Virement toEntity(VirementRequest request) {
        if (request == null) {
            return null;
        }
        return Virement.builder()
                .sourceAccount(request.getSourceAccount())
                .destinationAccount(request.getDestinationAccount())
                .montant(request.getMontant())
                .type(request.getType())
                .dateExecution(LocalDateTime.now())
                .statut("EN_COURS")
                .motif(request.getMotif())
                .build();
    }

    public VirementDTO toDTO(Virement entity) {
        if (entity == null) {
            return null;
        }
        return VirementDTO.builder()
                .id(entity.getId())
                .sourceAccount(entity.getSourceAccount())
                .destinationAccount(entity.getDestinationAccount())
                .montant(entity.getMontant())
                .type(entity.getType())
                .dateExecution(entity.getDateExecution())
                .statut(entity.getStatut())
                .motif(entity.getMotif())
                .build();
    }

    public VirementResponse toResponse(Virement entity) {
        if (entity == null) {
            return null;
        }
        return VirementResponse.builder()
                .id(entity.getId())
                .sourceAccount(entity.getSourceAccount())
                .destinationAccount(entity.getDestinationAccount())
                .montant(entity.getMontant())
                .type(entity.getType())
                .dateExecution(entity.getDateExecution())
                .statut(entity.getStatut())
                .motif(entity.getMotif())
                .build();
    }

    public void updateEntity(Virement entity, VirementRequest request) {
        if (entity == null || request == null) {
            return;
        }
        entity.setSourceAccount(request.getSourceAccount());
        entity.setDestinationAccount(request.getDestinationAccount());
        entity.setMontant(request.getMontant());
        entity.setType(request.getType());
        entity.setMotif(request.getMotif());
    }
}
