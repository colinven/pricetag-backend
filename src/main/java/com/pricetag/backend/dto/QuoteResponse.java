package com.pricetag.backend.dto;

public record QuoteResponse(
        LookupResult lookupResult,
        Integer[] price,
        String address
) {}
