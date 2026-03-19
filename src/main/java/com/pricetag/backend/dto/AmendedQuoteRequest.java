package com.pricetag.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AmendedQuoteRequest(
        @NotNull PropertyData data,
        @NotBlank String lastWash
) {
}
