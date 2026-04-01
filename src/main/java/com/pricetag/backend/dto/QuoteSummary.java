package com.pricetag.backend.dto;

import com.pricetag.backend.entity.Quote;

import java.time.LocalDateTime;
import java.util.UUID;

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
