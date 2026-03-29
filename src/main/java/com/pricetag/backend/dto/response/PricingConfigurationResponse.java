package com.pricetag.backend.dto.response;

import lombok.Builder;

@Builder
public record PricingConfigurationResponse(
        Double baseSqftPrice,
        Double storyMultiplier,
        Integer minimumPrice,
        Integer priceRangeBuffer,
        Integer quoteExpiryDays
) {
}
