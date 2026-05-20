package com.training.mybank.exception;

public class InvalidRecoveryDetailsException extends BankingException {
    public InvalidRecoveryDetailsException(String message) {
        super(message);
    }
}
