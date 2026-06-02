package com.test.digitalbankapi.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountRequestDTO(
        @NotBlank(message = "Owner name is required")
        @Size(min = 3, max = 100, message = "Owner name must be between 3 and 100 characters")
        String ownerName,

        @NotNull(message = "Initial balance is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance cannot be negative")
        BigDecimal initialBalance
) {}