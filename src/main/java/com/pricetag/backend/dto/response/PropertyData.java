package com.pricetag.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PropertyData(
        Integer sqft,
        @JsonProperty("year_built") Integer yearBuilt,
        Integer stories,
        Integer garage,
        @JsonProperty("property_type") String propertyType
) {}
