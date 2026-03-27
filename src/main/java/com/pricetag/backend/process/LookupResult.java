package com.pricetag.backend.process;

import com.pricetag.backend.dto.response.PropertyData;

public record LookupResult(
        PropertyData data,
        String message
) {
}