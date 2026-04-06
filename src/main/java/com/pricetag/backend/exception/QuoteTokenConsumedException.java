package com.pricetag.backend.exception;

public class QuoteTokenConsumedException extends RuntimeException {
    public QuoteTokenConsumedException() {
        super("Token has already been consumed.");
    }
}
