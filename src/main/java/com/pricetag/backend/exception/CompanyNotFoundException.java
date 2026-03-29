package com.pricetag.backend.exception;

import java.util.UUID;

public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(UUID companyId) {
        super("Company with id " + companyId + " not found");
    }
}
