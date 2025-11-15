package com.bank.beneficiaireservice.exception;

public class BeneficiaireNotFoundException extends RuntimeException {
    public BeneficiaireNotFoundException(Long id) {
        super("Beneficiaire not found with id " + id);
    }

    public BeneficiaireNotFoundException(String rib) {
        super("Beneficiaire not found with RIB " + rib);
    }
}
