package com.pricetag.backend.dto.response;

import com.pricetag.backend.dto.QuoteSummary;

import java.util.List;

public record PendingQuotesResponse(
        List<QuoteSummary> pendingQuotes
) {
}
