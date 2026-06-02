package com.test.digitalbankapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountRequestDTO(
        @Schema(description = "Nome do titular da conta", example = "Gabriel Siqueira")
        @NotBlank(message = "Owner name is required")
        @Size(min = 3, max = 100, message = "Owner name must be between 3 and 100 characters")
        String ownerName,

        @Schema(description = "Saldo inicial para abertura da conta", example = "1000.00")
        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance cannot be negative")
        BigDecimal initialBalance
) {}