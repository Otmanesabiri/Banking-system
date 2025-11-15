package com.bank.virementservice.dto;

import com.bank.virementservice.model.TypeVirement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VirementRequest {

    @NotBlank
    private String sourceAccount;

    @NotBlank
    private String destinationAccount;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal montant;

    @NotNull
    private TypeVirement type;

    private String motif;
}
