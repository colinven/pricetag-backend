package com.pricetag.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PricingConfigurationRequest(
        @NotNull @DecimalMin("0.1") Double baseSqftPrice,
        @NotNull @DecimalMin("0.1") Double storyMultiplier,
        @NotNull @Min(1) Integer minimumPrice,
        @NotNull @Min(1) Integer priceRangeBuffer,
        @NotNull @Min(1) @Max(365) Integer quoteExpiryDays
) {}
