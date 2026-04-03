package com.pricetag.backend.exception;

public class InvalidQuoteStatusException extends RuntimeException {
    public InvalidQuoteStatusException(String message) {
        super(message);
    }
}
