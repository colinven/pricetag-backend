package com.pricetag.backend.exception;

import java.util.UUID;

public class PricingNotConfiguredException extends RuntimeException {
    public PricingNotConfiguredException() {
        super("Company does not have pricing configured.");
    }
}
