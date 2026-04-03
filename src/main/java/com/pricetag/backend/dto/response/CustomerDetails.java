package com.pricetag.backend.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record CustomerDetails(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDateTime createdAt,
        List<QuoteSummary> quotes
) {
}
