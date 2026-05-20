package com.training.mybank.exception;

public class BankingException extends RuntimeException {

    public BankingException(String message) {
        super(message);
    }
}
