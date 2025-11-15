package com.bank.virementservice.exception;

public class VirementNotFoundException extends RuntimeException {
    public VirementNotFoundException(Long id) {
        super("Virement not found with id " + id);
    }
}
