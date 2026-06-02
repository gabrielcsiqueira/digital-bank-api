package com.test.digitalbankapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferRequestDTO(
        @Schema(description = "ID da conta de origem (quem vai enviar o dinheiro)", example = "1")
        @NotNull(message = "Source account ID is required")
        Long sourceAccountId,

        @Schema(description = "ID da conta de destino (quem vai receber o dinheiro)", example = "2")
        @NotNull(message = "Destination account ID is required")
        Long destinationAccountId,

        @Schema(description = "Valor monetário a ser transferido", example = "250.00")
        @NotNull(message = "Transfer amount is required")
        @Positive(message = "Transfer amount must be greater than zero")
        BigDecimal amount
) {}