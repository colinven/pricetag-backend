package com.pricetag.backend.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record APIError(
        LocalDateTime timestamp,
        int statusCode,
        String error,
        Object message
) {
}
