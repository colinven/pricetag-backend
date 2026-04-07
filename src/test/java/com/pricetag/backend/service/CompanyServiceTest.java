package com.pricetag.backend.service;

import com.pricetag.backend.dto.response.CompanySummary;
import com.pricetag.backend.entity.Company;
import com.pricetag.backend.exception.CompanyNotFoundException;
import com.pricetag.backend.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {

    @Mock private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    private static final String SLUG = "test-company";
    private Company company;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(UUID.randomUUID());
        company.setSlug(SLUG);
        company.setName("Test Company");
        company.setEmail("test@company.com");
        company.setPhone("555-9999");
    }

    @Test
    void givenValidSlug_whenGetCompanySummaryFromSlug_thenReturnsPopulatedCompanySummary() {
        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));

        CompanySummary result = companyService.getCompanySummaryFromSlug(SLUG);

        assertThat(result.companyName()).isEqualTo("Test Company");
        assertThat(result.companyEmail()).isEqualTo("test@company.com");
        assertThat(result.companyPhone()).isEqualTo("555-9999");
    }

    @Test
    void givenUnknownSlug_whenGetCompanySummaryFromSlug_thenThrowsCompanyNotFoundException() {
        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getCompanySummaryFromSlug(SLUG))
                .isInstanceOf(CompanyNotFoundException.class);
    }
}
