package com.pricetag.backend.exception;

import java.util.UUID;

public class QuoteNotFoundException extends RuntimeException {
    public QuoteNotFoundException(UUID quoteId) {
        super("Quote with id " + quoteId + " not found");
    }
}
