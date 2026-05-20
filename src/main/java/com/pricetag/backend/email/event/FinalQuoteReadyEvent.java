package com.pricetag.backend.email.event;

import com.pricetag.backend.entity.Company;
import com.pricetag.backend.entity.CompanyPricing;
import com.pricetag.backend.entity.Customer;
import com.pricetag.backend.entity.Property;

import java.util.UUID;

public record FinalQuoteReadyEvent(
        UUID quoteId,
        String quoteToken,
        Company company,
        CompanyPricing pricing,
        Customer customer,
        Property property
) {
}
