package com.pricetag.backend.dto;

public record LookupResult(
        PropertyData data,
        String message
) {
}