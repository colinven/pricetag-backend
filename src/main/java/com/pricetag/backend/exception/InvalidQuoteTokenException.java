package com.pricetag.backend.exception;

public class InvalidQuoteTokenException extends RuntimeException {
    public InvalidQuoteTokenException() {super("The requested resource could not be found.");}
}
