package com.hospitalmanagement.dto;

import java.time.Instant;

public record ApiErrorResponse(
        int status,
        String message,
        Instant timestamp
) {
}
