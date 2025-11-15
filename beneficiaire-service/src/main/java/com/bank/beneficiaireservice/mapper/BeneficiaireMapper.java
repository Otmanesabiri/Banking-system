package com.bank.beneficiaireservice.mapper;

import com.bank.beneficiaireservice.dto.BeneficiaireDTO;
import com.bank.beneficiaireservice.dto.BeneficiaireRequest;
import com.bank.beneficiaireservice.dto.BeneficiaireResponse;
import com.bank.beneficiaireservice.model.Beneficiaire;
import org.springframework.stereotype.Component;

@Component
public class BeneficiaireMapper {

    public Beneficiaire toEntity(BeneficiaireRequest request) {
        if (request == null) {
            return null;
        }
        return Beneficiaire.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .rib(request.getRib())
                .type(request.getType())
                .build();
    }

    public BeneficiaireDTO toDTO(Beneficiaire entity) {
        if (entity == null) {
            return null;
        }
        return BeneficiaireDTO.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .rib(entity.getRib())
                .type(entity.getType())
                .build();
    }

    public BeneficiaireResponse toResponse(Beneficiaire entity) {
        if (entity == null) {
            return null;
        }
        return BeneficiaireResponse.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .rib(entity.getRib())
                .type(entity.getType())
                .build();
    }

    public void updateEntity(Beneficiaire entity, BeneficiaireRequest request) {
        if (entity == null || request == null) {
            return;
        }
        entity.setNom(request.getNom());
        entity.setPrenom(request.getPrenom());
        entity.setRib(request.getRib());
        entity.setType(request.getType());
    }
}
