package com.test.digitalbankapi.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(BigDecimal balance, BigDecimal amount) {
        super("Insufficient balance. Available: " + balance + ", required: " + amount);
    }
}
