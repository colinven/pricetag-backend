package com.pricetag.backend.exception;

public class EmailSendException extends RuntimeException {
    public EmailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
