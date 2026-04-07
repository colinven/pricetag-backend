package com.pricetag.backend.dto.response;

import lombok.Builder;

@Builder
public record CompanySummary(
        String companyName,
        String companyPhone,
        String companyEmail
) {
}
