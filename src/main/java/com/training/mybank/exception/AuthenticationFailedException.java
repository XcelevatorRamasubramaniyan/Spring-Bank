package com.training.mybank.exception;

public class AuthenticationFailedException extends BankingException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
