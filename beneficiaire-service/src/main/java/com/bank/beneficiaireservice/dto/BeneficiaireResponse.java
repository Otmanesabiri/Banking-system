package com.bank.beneficiaireservice.dto;

import com.bank.beneficiaireservice.model.TypeBeneficiaire;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeneficiaireResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String rib;
    private TypeBeneficiaire type;
}
