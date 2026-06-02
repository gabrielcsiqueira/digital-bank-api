package com.test.digitalbankapi.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponseDTO(
        Long transferId,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        LocalDateTime createdAt
) {}