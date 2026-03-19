package com.pricetag.backend.dto;

public record ContactInfo(
        String firstName,
        String lastName,
        String phone,
        String email
) {
}
