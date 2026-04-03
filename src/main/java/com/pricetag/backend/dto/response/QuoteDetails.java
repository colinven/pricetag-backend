package com.pricetag.backend.dto.response;

import com.pricetag.backend.entity.Quote;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record QuoteDetails(
        UUID id,
        String customerFirstName,
        String customerLastName,
        String customerEmail,
        String customerPhone,
        String propertyAddress,
        Integer propertySqft,
        Integer propertyStories,
        Integer propertyYearBuilt,
        Integer propertyGarageSize,
        String propertyType,
        Integer priceLow,
        Integer priceHigh,
        Integer finalPrice,
        Quote.Status status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        LocalDateTime acceptedAt,
        LocalDateTime declinedAt,
        LocalDateTime expiresAt
) {
}
