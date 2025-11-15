package com.bank.virementservice.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String account, String message) {
        super("Insufficient funds for account " + account + ": " + message);
    }
}
