package com.pricetag.backend.service;

import com.pricetag.backend.dto.response.CompanySummary;
import com.pricetag.backend.entity.Company;
import com.pricetag.backend.exception.CompanyNotFoundException;
import com.pricetag.backend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanySummary getCompanySummaryFromSlug(String slug) {
        Company company = companyRepository.findBySlug(slug)
                .orElseThrow(() -> new CompanyNotFoundException(slug));

        return CompanySummary.builder()
                .companyName(company.getName())
                .companyPhone(company.getPhone())
                .companyEmail(company.getEmail())
                .build();
    }
}
