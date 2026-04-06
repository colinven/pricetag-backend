package com.pricetag.backend.dto.request;

import com.pricetag.backend.entity.Quote;
import jakarta.validation.constraints.NotNull;

public record CustomerQuoteDecision(
        @NotNull Quote.Status status
) {
}
