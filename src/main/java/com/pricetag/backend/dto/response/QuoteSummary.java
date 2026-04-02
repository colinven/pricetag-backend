package com.pricetag.backend.dto.response;

import com.pricetag.backend.entity.Quote;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record QuoteSummary(
        UUID id,
        Quote.Status status,
        String customerFirstName,
        String customerLastName,
        String propertyAddress,
        Integer priceLow,
        Integer priceHigh,
        LocalDateTime createdAt
) {
}
