package com.pricetag.backend.dto.response;

import com.pricetag.backend.process.LookupResult;

public record QuoteResponse(
        LookupResult lookupResult,
        Integer[] price,
        String address
) {}
