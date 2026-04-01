package com.pricetag.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value= BadCredentialsException.class)
    public ResponseEntity<APIError> handleBadCredentialsException() {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        APIError error = APIError.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(status.value())
                .error(status.getReasonPhrase())
                .message("Username or password is incorrect")
                .build();
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(value= EmailAlreadyExistsException.class)
    public ResponseEntity<APIError> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        APIError error = APIError.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<APIError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((fieldError) ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage())
                );
        APIError error = APIError.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(status.value())
                .error(status.getReasonPhrase())
                .message(errors)
                .build();
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(value = CompanyNotFoundException.class)
    public ResponseEntity<APIError> handleCompanyNotFoundException(CompanyNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        APIError error = APIError.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(value = CustomerNotFoundException.class)
    public ResponseEntity<APIError> handleCustomerNotFoundException(CompanyNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        APIError error = APIError.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(value = PricingNotConfiguredException.class)
    public ResponseEntity<APIError> handlePricingNotConfiguredException(PricingNotConfiguredException ex) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        APIError error = APIError.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(value = OutOfServiceAreaException.class)
    public ResponseEntity<APIError> handleOutOfServiceAreaException(OutOfServiceAreaException ex) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        APIError error = APIError.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(value = GeocodingException.class)
    public ResponseEntity<APIError> handleGeocodingException(GeocodingException ex) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        APIError error = APIError.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
