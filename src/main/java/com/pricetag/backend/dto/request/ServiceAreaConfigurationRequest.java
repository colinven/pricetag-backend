package com.pricetag.backend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ServiceAreaConfigurationRequest(
        @NotNull @Min(1) Integer serviceRadiusMiles,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double serviceAreaLatitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double serviceAreaLongitude
) {
}
