package com.test.digitalbankapi.exception;

public class InvalidTransferAmountException extends BusinessException {
    public InvalidTransferAmountException() {
        super("Transfer amount must be greater than zero.");
    }
}