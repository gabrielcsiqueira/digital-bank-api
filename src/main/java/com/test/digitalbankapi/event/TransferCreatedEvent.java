package com.test.digitalbankapi.event;

import java.math.BigDecimal;

public record TransferCreatedEvent(
        Long transferId,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount
) {}