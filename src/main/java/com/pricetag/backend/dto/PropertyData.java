package com.pricetag.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PropertyData(
        Integer sqft,
        @JsonProperty("year_built") Integer yearBuilt,
        Integer stories,
        Integer garage,
        @JsonProperty("property_type") String propertyType
) {}
