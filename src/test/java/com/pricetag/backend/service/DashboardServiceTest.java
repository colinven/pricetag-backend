package com.pricetag.backend.service;

import com.pricetag.backend.dto.response.*;
import com.pricetag.backend.entity.Company;
import com.pricetag.backend.entity.Customer;
import com.pricetag.backend.entity.Property;
import com.pricetag.backend.entity.Quote;
import com.pricetag.backend.exception.CompanyNotFoundException;
import com.pricetag.backend.exception.CustomerNotFoundException;
import com.pricetag.backend.exception.QuoteNotFoundException;
import com.pricetag.backend.repository.CompanyRepository;
import com.pricetag.backend.repository.CustomerRepository;
import com.pricetag.backend.repository.QuoteRepository;
import com.pricetag.backend.entity.QuoteToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private QuoteRepository quoteRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private QuoteTokenService quoteTokenService;
    @Mock private EmailService emailService;

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
        when(quoteRepository.countByCompanyIdAndStatusIn(eq(companyId), any())).thenReturn(20);
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
        when(quoteRepository.countByCompanyIdAndStatusIn(eq(companyId), any())).thenReturn(0);
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
        when(quoteRepository.countByCompanyIdAndStatusIn(eq(companyId), any())).thenReturn(10);
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
        when(quoteRepository.countByCompanyIdAndStatusIn(eq(companyId), any())).thenReturn(0);
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
        when(quoteRepository.countByCompanyIdAndStatusIn(eq(companyId), any())).thenReturn(5);
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
        when(quoteRepository.countByCompanyIdAndStatusIn(eq(companyId), any())).thenReturn(3);
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
    void givenValidQuoteAndValidCompany_whenFinalizeQuote_thenUpdatesAndSavesQuote() throws Exception {
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
        when(quoteTokenService.generateToken(quote)).thenReturn("fake-raw-token");

        FinalizedQuoteResponse response = dashboardService.finalizeQuote(companyId, quote.getId(), finalPrice);

        assertThat(quote.getFinalPrice()).isEqualTo(finalPrice);
        assertThat(quote.getStatus()).isEqualTo(Quote.Status.REVIEWED);
        assertThat(quote.getReviewedAt()).isNotNull();
        assertThat(response.quoteId()).isEqualTo(quote.getId());
        assertThat(response.status()).isEqualTo(quote.getStatus());
        assertThat(response.finalPrice()).isEqualTo(quote.getFinalPrice());

        Mockito.verify(quoteRepository, Mockito.times(1)).save(quote);
        verify(quoteTokenService).generateToken(quote);
        verify(emailService).sendLinkToQuoteEmail(quote, "fake-raw-token");
    }

    @Test
    void givenValidQuote_whenFinalizeQuote_thenRawTokenPassedToEmailNotHash() throws Exception {
        // The raw token returned by generateToken must be passed to emailService unchanged —
        // the hash must never reach the email link.
        Quote quote = Quote.builder()
                .id(UUID.randomUUID())
                .company(company)
                .customer(new Customer())
                .property(new Property())
                .status(Quote.Status.PENDING)
                .build();
        String expectedRawToken = "the-raw-token-for-email";
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(quoteRepository.findById(quote.getId())).thenReturn(Optional.of(quote));
        when(quoteTokenService.generateToken(quote)).thenReturn(expectedRawToken);

        dashboardService.finalizeQuote(companyId, quote.getId(), 400);

        verify(emailService).sendLinkToQuoteEmail(quote, expectedRawToken);
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

    // getCustomers()

    @Test
    void givenUnknownCompanyId_whenGetCustomers_thenThrowsCompanyNotFoundException() {
        when(companyRepository.existsById(companyId)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getCustomers(companyId, 0, 10, "createdAt", "desc"))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void givenAscSortDirection_whenGetCustomers_thenPageableUsesAscendingSort() {
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(customerRepository.findAllByCompanyId(eq(companyId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        dashboardService.getCustomers(companyId, 0, 10, "firstName", "asc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(customerRepository).findAllByCompanyId(eq(companyId), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("firstName").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void givenDescSortDirection_whenGetCustomers_thenPageableUsesDescendingSort() {
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(customerRepository.findAllByCompanyId(eq(companyId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        dashboardService.getCustomers(companyId, 0, 10, "lastName", "desc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(customerRepository).findAllByCompanyId(eq(companyId), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("lastName").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void givenUnknownSortBy_whenGetCustomers_thenDefaultsToCreatedAt() {
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(customerRepository.findAllByCompanyId(eq(companyId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        dashboardService.getCustomers(companyId, 0, 10, "invalidField", "asc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(customerRepository).findAllByCompanyId(eq(companyId), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    void givenValidRequest_whenGetCustomers_thenReturnsPageFromRepository() {
        CustomerSummary summary = new CustomerSummary(UUID.randomUUID(), "Jane", "Smith", LocalDateTime.now());
        Page<CustomerSummary> expectedPage = new PageImpl<>(List.of(summary));
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(customerRepository.findAllByCompanyId(eq(companyId), any(Pageable.class))).thenReturn(expectedPage);

        Page<CustomerSummary> result = dashboardService.getCustomers(companyId, 0, 10, "createdAt", "desc");

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst()).isEqualTo(summary);
    }

    // getCustomerDetailsAndQuotesById()

    @Test
    void givenUnknownCompanyId_whenGetCustomerDetails_thenThrowsCompanyNotFoundException() {
        when(companyRepository.existsById(companyId)).thenReturn(false);

        assertThatThrownBy(() -> dashboardService.getCustomerDetailsAndQuotesById(companyId, UUID.randomUUID()))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void givenUnknownCustomerId_whenGetCustomerDetails_thenThrowsCustomerNotFoundException() {
        UUID customerId = UUID.randomUUID();
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(customerRepository.findByIdAndCompanyId(customerId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getCustomerDetailsAndQuotesById(companyId, customerId))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void givenCustomerBelongingToDifferentCompany_whenGetCustomerDetails_thenThrowsCustomerNotFoundException() {
        // findByIdAndCompanyId returns empty when the customer's company doesn't match — prevents BOLA
        UUID customerId = UUID.randomUUID();
        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(customerRepository.findByIdAndCompanyId(customerId, companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getCustomerDetailsAndQuotesById(companyId, customerId))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void givenValidIds_customerHasQuotes_whenGetCustomerDetails_thenReturnsDetailsWithQuotes() {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(customerId)
                .firstName("Jane").lastName("Smith")
                .email("jane@example.com").phone("555-9876")
                .createdAt(LocalDateTime.now())
                .build();
        QuoteSummary q1 = new QuoteSummary(UUID.randomUUID(), Quote.Status.PENDING,
                "Jane", "Smith", "456 Oak Ave", 150, 250, null, LocalDateTime.now());
        QuoteSummary q2 = new QuoteSummary(UUID.randomUUID(), Quote.Status.REVIEWED,
                "Jane", "Smith", "789 Pine Rd", 200, 300, 250, LocalDateTime.now().minusDays(5));

        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(customerRepository.findByIdAndCompanyId(customerId, companyId)).thenReturn(Optional.of(customer));
        when(quoteRepository.findAllByCustomerIdAndCompanyIdOrderByCreatedAtDesc(customerId, companyId))
                .thenReturn(List.of(q1, q2));

        CustomerDetails result = dashboardService.getCustomerDetailsAndQuotesById(companyId, customerId);

        assertThat(result.id()).isEqualTo(customerId);
        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.lastName()).isEqualTo("Smith");
        assertThat(result.email()).isEqualTo("jane@example.com");
        assertThat(result.phone()).isEqualTo("555-9876");
        assertThat(result.quotes().size()).isEqualTo(2);
    }

    @Test
    void givenValidIds_customerHasNoQuotes_whenGetCustomerDetails_thenReturnsDetailsWithEmptyQuotes() {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(customerId)
                .firstName("Jane").lastName("Smith")
                .email("jane@example.com").phone("555-9876")
                .createdAt(LocalDateTime.now())
                .build();

        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(customerRepository.findByIdAndCompanyId(customerId, companyId)).thenReturn(Optional.of(customer));
        when(quoteRepository.findAllByCustomerIdAndCompanyIdOrderByCreatedAtDesc(customerId, companyId))
                .thenReturn(List.of());

        CustomerDetails result = dashboardService.getCustomerDetailsAndQuotesById(companyId, customerId);

        assertThat(result.quotes().isEmpty()).isTrue();
    }
}
