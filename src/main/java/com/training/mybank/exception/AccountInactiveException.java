package com.training.mybank.exception;

public class AccountInactiveException extends BankingException {
    public AccountInactiveException(String message) {
        super(message);
    }
}
