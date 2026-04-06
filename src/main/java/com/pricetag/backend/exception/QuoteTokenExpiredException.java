package com.pricetag.backend.exception;

public class QuoteTokenExpiredException extends RuntimeException {
    public QuoteTokenExpiredException() {super("Token has expired");}
}
