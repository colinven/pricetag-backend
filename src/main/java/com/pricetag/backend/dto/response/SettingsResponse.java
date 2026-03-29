package com.pricetag.backend.dto.response;

import lombok.Builder;

@Builder
public record SettingsResponse(
        PricingConfigurationResponse pricingConfig,
        ServiceAreaConfigurationResponse serviceArea
) {}
