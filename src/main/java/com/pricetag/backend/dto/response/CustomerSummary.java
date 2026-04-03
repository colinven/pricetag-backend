package com.pricetag.backend.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CustomerSummary(
        UUID id,
        String firstName,
        String lastName,
        LocalDateTime createdAt
) {
}
