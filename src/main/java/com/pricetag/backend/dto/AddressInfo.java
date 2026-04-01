package com.pricetag.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressInfo(
        @NotBlank @Pattern(regexp = "^\\d+\\s+\\S+.*") String street,
        @NotBlank String city,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String state,
        @NotBlank @Pattern(regexp = "^\\d{5}$") String zip
) {
}
