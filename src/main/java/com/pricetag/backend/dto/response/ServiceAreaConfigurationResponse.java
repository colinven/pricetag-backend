package com.pricetag.backend.dto.response;

import lombok.Builder;

@Builder
public record ServiceAreaConfigurationResponse(
        Integer serviceRadiusMiles,
        Double serviceAreaLatitude,
        Double serviceAreaLongitude
) {
}
