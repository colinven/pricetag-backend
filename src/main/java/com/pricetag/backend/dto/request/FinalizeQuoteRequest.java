package com.pricetag.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FinalizeQuoteRequest(
        @NotNull @Positive Integer finalPrice
) {
}
