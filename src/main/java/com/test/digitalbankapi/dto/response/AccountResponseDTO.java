package com.test.digitalbankapi.dto.response;

import java.math.BigDecimal;

public record AccountResponseDTO(
        Long id,
        String ownerName,
        BigDecimal balance
) {}
