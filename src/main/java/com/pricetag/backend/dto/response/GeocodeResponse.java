package com.pricetag.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodeResponse(
        List<GeocodeResult> results,
        String status
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeocodeResult(
         Geometry geometry
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(
            Location location
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(
            double lat,
            double lng
    ){}
}
