package com.pricetag.backend.service;

import com.pricetag.backend.dto.response.DashboardSummaryResponse;
import com.pricetag.backend.entity.Company;
import com.pricetag.backend.entity.Quote;
import com.pricetag.backend.exception.CompanyNotFoundException;
import com.pricetag.backend.repository.CompanyRepository;
import com.pricetag.backend.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private QuoteRepository quoteRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private UUID companyId;
    private Company company;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        company = new Company();
        company.setId(companyId);
    }

    @Test
    void givenUnknownCompanyId_whenGetDashboard_thenThrowsCompanyNotFoundException() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getDashboardSummary(companyId))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void givenNormalData_whenGetDashboard_thenReturnsSummaryWithCorrectValues() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(quoteRepository.countByCompanyId(companyId)).thenReturn(50);
        when(quoteRepository.countByCompanyIdAndCreatedAtAfter(eq(companyId), any())).thenReturn(10);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.PENDING)).thenReturn(5);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.REVIEWED)).thenReturn(20);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.ACCEPTED)).thenReturn(10);
        when(quoteRepository.findFinalPricesByCompanyIdAndStatusIn(eq(companyId), any())).thenReturn(List.of(100, 200, 300));

        DashboardSummaryResponse response = dashboardService.getDashboardSummary(companyId);

        assertThat(response.totalNumOfQuotes()).isEqualTo(50);
        assertThat(response.numOfQuotesThirtyDays()).isEqualTo(10);
        assertThat(response.numOfQuotesToReview()).isEqualTo(5);
        assertThat(response.conversionRate()).isEqualTo(0.5);
        assertThat(response.averageFinalPrice()).isEqualTo(200.0);
    }

    @Test
    void givenNoReviewedQuotes_whenGetDashboard_thenConversionRateIsZero() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(quoteRepository.countByCompanyId(companyId)).thenReturn(5);
        when(quoteRepository.countByCompanyIdAndCreatedAtAfter(eq(companyId), any())).thenReturn(5);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.PENDING)).thenReturn(5);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.REVIEWED)).thenReturn(0);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.ACCEPTED)).thenReturn(0);
        when(quoteRepository.findFinalPricesByCompanyIdAndStatusIn(eq(companyId), any())).thenReturn(List.of());

        DashboardSummaryResponse response = dashboardService.getDashboardSummary(companyId);

        assertThat(response.conversionRate()).isEqualTo(0.0);
    }

    @Test
    void givenNoAcceptedQuotes_whenGetDashboard_thenConversionRateIsZero() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(quoteRepository.countByCompanyId(companyId)).thenReturn(10);
        when(quoteRepository.countByCompanyIdAndCreatedAtAfter(eq(companyId), any())).thenReturn(10);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.PENDING)).thenReturn(0);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.REVIEWED)).thenReturn(10);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.ACCEPTED)).thenReturn(0);
        when(quoteRepository.findFinalPricesByCompanyIdAndStatusIn(eq(companyId), any())).thenReturn(List.of());

        DashboardSummaryResponse response = dashboardService.getDashboardSummary(companyId);

        assertThat(response.conversionRate()).isEqualTo(0.0);
    }

    @Test
    void givenNoFinalPrices_whenGetDashboard_thenAverageFinalPriceIsZero() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(quoteRepository.countByCompanyId(companyId)).thenReturn(5);
        when(quoteRepository.countByCompanyIdAndCreatedAtAfter(eq(companyId), any())).thenReturn(5);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.PENDING)).thenReturn(5);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.REVIEWED)).thenReturn(0);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.ACCEPTED)).thenReturn(0);
        when(quoteRepository.findFinalPricesByCompanyIdAndStatusIn(eq(companyId), any())).thenReturn(List.of());

        DashboardSummaryResponse response = dashboardService.getDashboardSummary(companyId);

        assertThat(response.averageFinalPrice()).isEqualTo(0.0);
    }

    @Test
    void givenFinalPricesWithNulls_whenGetDashboard_thenAverageIgnoresNulls() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(quoteRepository.countByCompanyId(companyId)).thenReturn(10);
        when(quoteRepository.countByCompanyIdAndCreatedAtAfter(eq(companyId), any())).thenReturn(10);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.PENDING)).thenReturn(0);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.REVIEWED)).thenReturn(5);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.ACCEPTED)).thenReturn(5);
        when(quoteRepository.findFinalPricesByCompanyIdAndStatusIn(eq(companyId), any()))
                .thenReturn(java.util.Arrays.asList(100, null, 300, null, 500));

        DashboardSummaryResponse response = dashboardService.getDashboardSummary(companyId);

        assertThat(response.averageFinalPrice()).isEqualTo(300.0);
    }

    @Test
    void givenAllFinalPricesNull_whenGetDashboard_thenAverageFinalPriceIsZero() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(quoteRepository.countByCompanyId(companyId)).thenReturn(3);
        when(quoteRepository.countByCompanyIdAndCreatedAtAfter(eq(companyId), any())).thenReturn(3);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.PENDING)).thenReturn(0);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.REVIEWED)).thenReturn(3);
        when(quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.ACCEPTED)).thenReturn(0);
        when(quoteRepository.findFinalPricesByCompanyIdAndStatusIn(eq(companyId), any()))
                .thenReturn(java.util.Arrays.asList(null, null, null));

        DashboardSummaryResponse response = dashboardService.getDashboardSummary(companyId);

        assertThat(response.averageFinalPrice()).isEqualTo(0.0);
    }
}
