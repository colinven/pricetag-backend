package com.pricetag.backend.dto.response;

import java.util.List;

public record QuotesResponse(
        List<QuoteSummary> quotes
) {
}
