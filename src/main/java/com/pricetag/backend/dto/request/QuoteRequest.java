package com.pricetag.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QuoteRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String phone,
        @Email @NotBlank String email,
        @NotBlank @Pattern(regexp = "^\\d+\\s+\\S+.*") String street,
        @NotBlank String city,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String state,
        @NotBlank @Pattern(regexp = "^\\d{5}$") String zip,
        @NotBlank String lastWash
) {}
