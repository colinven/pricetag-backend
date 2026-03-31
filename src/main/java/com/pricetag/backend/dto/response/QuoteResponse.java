package com.pricetag.backend.dto.response;

import com.pricetag.backend.process.LookupResult;
import lombok.Builder;

import java.util.UUID;

@Builder
public record QuoteResponse(
        UUID customerId,
        LookupResult lookupResult,
        Integer[] price,
        String address
) {}
