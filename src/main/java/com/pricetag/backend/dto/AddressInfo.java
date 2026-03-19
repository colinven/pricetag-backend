package com.pricetag.backend.dto;

public record AddressInfo(
        String street,
        String city,
        String state,
        String zip
) {
}
