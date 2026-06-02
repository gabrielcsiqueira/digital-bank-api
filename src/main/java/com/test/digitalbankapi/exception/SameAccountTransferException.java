package com.test.digitalbankapi.exception;

public class SameAccountTransferException extends BusinessException {
    public SameAccountTransferException() {
        super("Source and destination accounts must be different.");
    }
}