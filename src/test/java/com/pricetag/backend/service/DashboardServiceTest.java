package com.pricetag.backend.service;

import com.pricetag.backend.dto.response.FinalizedQuoteResponse;
import com.pricetag.backend.dto.response.QuoteSummary;
import com.pricetag.backend.dto.response.DashboardSummaryResponse;
import com.pricetag.backend.dto.response.QuotesResponse;
import com.pricetag.backend.entity.Company;
import com.pricetag.backend.entity.Customer;
import com.pricetag.backend.entity.Property;
import com.pricetag.backend.entity.Quote;
import com.pricetag.backend.exception.CompanyNotFoundException;
import com.pricetag.backend.exception.QuoteNotFoundException;
import com.pricetag.backend.repository.CompanyRepository;
import com.pricetag.backend.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

    // getDashboardSummary()

    @Test
    void givenUnknownCompanyId_whenGetDashboard_thenThrowsCompanyNotFoundException() {
        when(companyRepository.existsById(companyId)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getDashboardSummary(companyId))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void givenNormalData_whenGetDashboard_thenReturnsSummaryWithCorrectValues() {
        when(companyRepository.existsById(companyId)).thenReturn(true);
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
        when(companyRepository.existsById(companyId)).thenReturn(true);
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
        when(companyRepository.existsById(companyId)).thenReturn(true);
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
        when(companyRepository.existsById(companyId)).thenReturn(true);
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
        when(companyRepository.existsById(companyId)).thenReturn(true);
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
        when(companyRepository.existsById(companyId)).thenReturn(true);
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

    // getPendingQuotes()

    @Test
    void givenUnknownCompanyId_whenGetPendingQuotes_thenThrowsCompanyNotFoundException() {
        when(companyRepository.existsById(companyId)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getPendingQuotes(companyId))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void givenNoPendingQuotes_whenGetPendingQuotes_thenReturnsEmptyList() {
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(quoteRepository.findByCompanyIdAndStatusOrderByCreatedAtAsc(companyId, Quote.Status.PENDING))
                .thenReturn(List.of());

        QuotesResponse response = dashboardService.getPendingQuotes(companyId);

        assertThat(response.quotes().isEmpty()).isTrue();
    }

    @Test
    void givenPendingQuotes_whenGetPendingQuotes_thenReturnsQuotesInResponse() {
        QuoteSummary summary = new QuoteSummary(
                UUID.randomUUID(), Quote.Status.PENDING,
                "John", "Doe", "123 Main St", 100, 200, null, LocalDateTime.now()
        );
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(quoteRepository.findByCompanyIdAndStatusOrderByCreatedAtAsc(companyId, Quote.Status.PENDING))
                .thenReturn(List.of(summary));

        QuotesResponse response = dashboardService.getPendingQuotes(companyId);

        assertThat(response.quotes().size()).isEqualTo(1);
        assertThat(response.quotes().getFirst()).isEqualTo(summary);
    }

    // finalizeQuote()

    @Test
    void givenValidQuoteAndValidCompany_whenFinalizeQuote_thenUpdatesAndSavesQuote() {
        Quote quote = Quote.builder()
                .id(UUID.randomUUID())
                .company(company)
                .customer(new Customer())
                .property(new Property())
                .status(Quote.Status.PENDING)
                .build();
        int finalPrice = 350;
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(quoteRepository.findById(quote.getId())).thenReturn(Optional.of(quote));

        FinalizedQuoteResponse response = dashboardService.finalizeQuote(companyId, quote.getId(), finalPrice);

        assertThat(quote.getFinalPrice()).isEqualTo(finalPrice);
        assertThat(quote.getStatus()).isEqualTo(Quote.Status.REVIEWED);
        assertThat(quote.getReviewedAt()).isNotNull();
        assertThat(response.quoteId()).isEqualTo(quote.getId());
        assertThat(response.status()).isEqualTo(quote.getStatus());
        assertThat(response.finalPrice()).isEqualTo(quote.getFinalPrice());

        Mockito.verify(quoteRepository, Mockito.times(1)).save(quote);

    }

    @Test
    void givenInvalidCompanyId_whenFinalizeQuote_thenThrowsCompanyNotFoundException() {
        when(companyRepository.existsById(companyId)).thenReturn(false);
        assertThatThrownBy(() -> dashboardService.finalizeQuote(companyId, UUID.randomUUID(), 100))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void givenInvalidQuoteId_whenFinalizeQuote_thenThrowsQuoteNotFoundException() {
        when(companyRepository.existsById(companyId)).thenReturn(true);
        assertThatThrownBy(() -> dashboardService.finalizeQuote(companyId, UUID.randomUUID(), 100))
                .isInstanceOf(QuoteNotFoundException.class);
    }
}
