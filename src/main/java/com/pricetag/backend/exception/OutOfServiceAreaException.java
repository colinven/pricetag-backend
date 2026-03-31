package com.pricetag.backend.exception;

public class OutOfServiceAreaException extends RuntimeException {
    public OutOfServiceAreaException() {
        super("Requested property is out of this company's service area.");
    }
}
