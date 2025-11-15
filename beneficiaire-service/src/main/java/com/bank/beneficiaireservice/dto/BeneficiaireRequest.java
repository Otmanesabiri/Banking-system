package com.bank.beneficiaireservice.dto;

import com.bank.beneficiaireservice.model.TypeBeneficiaire;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BeneficiaireRequest {

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @NotBlank
    private String rib;

    @NotNull
    private TypeBeneficiaire type;
}
