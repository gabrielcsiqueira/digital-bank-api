package com.test.digitalbankapi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String type,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<String> details
) {}