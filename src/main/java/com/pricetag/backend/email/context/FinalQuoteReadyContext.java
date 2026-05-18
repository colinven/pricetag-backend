package com.pricetag.backend.email.context;

import java.util.Map;

public record FinalQuoteReadyContext(
        String companyName,
        String companyPhone,
        String companyEmail,
        String firstName,
        String customerEmail,
        String propertyAddress,
        String quoteExpiryDays,
        String finalQuoteUrl
) {
    public Map<String, String> toMap() {
        return Map.of(
                "companyName", companyName,
                "companyPhone", companyPhone,
                "companyEmail", companyEmail,
                "firstName", firstName,
                "propertyAddress", propertyAddress,
                "quoteExpiryDays", quoteExpiryDays,
                "finalQuoteUrl", finalQuoteUrl
        );
    }
}
