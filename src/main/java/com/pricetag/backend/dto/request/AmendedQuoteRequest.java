package com.pricetag.backend.dto.request;

import com.pricetag.backend.dto.response.PropertyData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AmendedQuoteRequest(
        @NotNull PropertyData data,
        @NotBlank String lastWash
) {
}
