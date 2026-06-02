package com.test.digitalbankapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        @Schema(example = "2026-06-02T17:21:17") LocalDateTime timestamp,
        @Schema(example = "400") Integer status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "One or more fields in the request are invalid.") String message,
        @Schema(example = "MethodArgumentNotValidException") String type,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "List of validation details or business rule flaws", example = "[\"Field 'amount': must not be null\"]")
        List<String> details
) {}