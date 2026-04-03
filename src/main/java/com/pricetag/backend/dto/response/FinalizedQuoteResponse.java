package com.pricetag.backend.dto.response;

import com.pricetag.backend.entity.Quote;
import lombok.Builder;

import java.util.UUID;

@Builder
public record FinalizedQuoteResponse(
        UUID quoteId,
        Integer finalPrice,
        Quote.Status status
) {
}
