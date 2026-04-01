package com.pricetag.backend.dto.request;

import com.pricetag.backend.dto.AddressInfo;
import com.pricetag.backend.dto.response.PropertyData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AmendedQuoteRequest(
        @NotNull UUID customerId,
        @NotNull PropertyData data,
        @NotNull AddressInfo addressInfo,
        @NotBlank String lastWash
) {
}
