package com.pricetag.backend.dto.response;

import lombok.Builder;

@Builder
public record DashboardSummaryResponse(
        int totalNumOfQuotes,
        int numOfQuotesThirtyDays,
        int numOfQuotesToReview,
        Double conversionRate,
        Double averageFinalPrice
) {
}
