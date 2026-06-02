package com.test.digitalbankapi.exception;

public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException(Long id) {
        super("Account not found: " + id);
    }
}