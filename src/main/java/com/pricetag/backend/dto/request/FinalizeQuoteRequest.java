package com.pricetag.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record FinalizeQuoteRequest(
        @NotNull Integer finalPrice
) {
}
