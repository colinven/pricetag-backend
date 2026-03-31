package com.pricetag.backend.exception;

public class GeocodingException extends RuntimeException {
    public GeocodingException(String address) {
        super("Could not geocode address: " + address);
    }
}
