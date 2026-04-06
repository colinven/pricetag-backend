package com.pricetag.backend.exception;

public class QuoteTokenMismatchException extends RuntimeException {
    public QuoteTokenMismatchException() {super("Invalid quote token");}
}
