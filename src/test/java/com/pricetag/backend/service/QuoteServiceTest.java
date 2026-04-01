package com.pricetag.backend.service;

import com.pricetag.backend.dto.AddressInfo;
import com.pricetag.backend.dto.request.AmendedQuoteRequest;
import com.pricetag.backend.dto.request.QuoteRequest;
import com.pricetag.backend.dto.response.AmendedPriceResponse;
import com.pricetag.backend.dto.response.PropertyData;
import com.pricetag.backend.dto.response.QuoteResponse;
import com.pricetag.backend.entity.*;
import com.pricetag.backend.exception.*;
import com.pricetag.backend.process.LookupProcess;
import com.pricetag.backend.process.LookupResult;
import com.pricetag.backend.repository.*;
import com.pricetag.backend.util.GeoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuoteServiceTest {

    @Mock private LookupProcess lookupProcess;
    @Mock private CustomerService customerService;
    @Mock private PropertyService propertyService;
    @Mock private PricingService pricingService;
    @Mock private GeoUtils geoUtils;
    @Mock private CompanyRepository companyRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private CompanyPricingRepository companyPricingRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private QuoteRepository quoteRepository;

    @InjectMocks
    private QuoteService quoteService;

    private static final String SLUG = "test-company";
    private static final String FORMATTED_ADDRESS = "123 Main St, Charlotte, NC 28202";

    private Company company;
    private CompanyPricing companyPricing;
    private Customer customer;
    private Property property;
    private QuoteRequest quoteRequest;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(UUID.randomUUID());
        company.setSlug(SLUG);

        companyPricing = new CompanyPricing();
        companyPricing.setQuoteExpiryDays(30);

        customer = Customer.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("555-1234")
                .build();

        property = Property.builder()
                .id(UUID.randomUUID())
                .fullAddress(FORMATTED_ADDRESS)
                .street("123 Main St")
                .city("Charlotte")
                .state("NC")
                .zip("28202")
                .sqft(2000)
                .numberOfStories(2)
                .yearBuilt(2000)
                .build();

        quoteRequest = new QuoteRequest(
                "John", "Doe", "555-1234", "john@example.com",
                "123 Main St", "Charlotte", "NC", "28202", "never"
        );
    }

    // getQuote

    @Test
    void givenUnknownSlug_whenGetQuote_thenThrowsCompanyNotFoundException() {
        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.getQuote(SLUG, quoteRequest))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void givenNoPricingConfigured_whenGetQuote_thenThrowsPricingNotConfiguredException() {
        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));
        when(companyPricingRepository.findByCompanyId(company.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.getQuote(SLUG, quoteRequest))
                .isInstanceOf(PricingNotConfiguredException.class);
    }

    @Test
    void givenOutOfServiceArea_whenGetQuote_thenThrowsOutOfServiceAreaException() {
        company.setServiceAreaLatitude(35.2271);
        company.setServiceAreaLongitude(-80.8431);
        company.setServiceRadiusMiles(30);

        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));
        when(companyPricingRepository.findByCompanyId(company.getId())).thenReturn(Optional.of(companyPricing));
        when(customerService.findOrCreate(quoteRequest)).thenReturn(customer);
        when(geoUtils.propertyIsInServiceArea(eq(company), anyString())).thenReturn(false);

        assertThatThrownBy(() -> quoteService.getQuote(SLUG, quoteRequest))
                .isInstanceOf(OutOfServiceAreaException.class);
    }

    @Test
    void givenServiceAreaNotConfigured_whenGetQuote_thenSkipsGeoCheck() {
        // company has no service area fields set — hasServiceAreaConfigured() returns false
        PropertyData data = PropertyData.builder().sqft(2000).stories(2).yearBuilt(2000).build();
        LookupResult lookupResult = new LookupResult(data, "ok");

        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));
        when(companyPricingRepository.findByCompanyId(company.getId())).thenReturn(Optional.of(companyPricing));
        when(customerService.findOrCreate(quoteRequest)).thenReturn(customer);
        when(propertyRepository.existsByFullAddress(FORMATTED_ADDRESS)).thenReturn(false);
        when(lookupProcess.startProcess(FORMATTED_ADDRESS)).thenReturn(lookupResult);
        when(pricingService.getPrice(any(), eq(data), anyString())).thenReturn(new Integer[]{100, 150});
        when(propertyRepository.findByFullAddress(FORMATTED_ADDRESS)).thenReturn(Optional.empty());
        when(propertyService.createProperty(anyString(), any(), any())).thenReturn(property);

        quoteService.getQuote(SLUG, quoteRequest);

        verifyNoInteractions(geoUtils);
    }

    @Test
    void givenPropertyInDb_withActiveQuoteForSameCustomer_whenGetQuote_thenReturnsExistingPriceWithoutNewQuote() {
        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));
        when(companyPricingRepository.findByCompanyId(company.getId())).thenReturn(Optional.of(companyPricing));
        when(customerService.findOrCreate(quoteRequest)).thenReturn(customer);
        when(propertyRepository.existsByFullAddress(FORMATTED_ADDRESS)).thenReturn(true);
        when(propertyRepository.findByFullAddress(FORMATTED_ADDRESS)).thenReturn(Optional.of(property));
        when(propertyService.getDataFromExistingProperty(property)).thenReturn(
                PropertyData.builder().sqft(2000).stories(2).yearBuilt(2000).build()
        );

        Quote existingQuote = Quote.builder()
                .customer(customer)
                .property(property)
                .priceLow(200)
                .priceHigh(300)
                .expiresAt(LocalDateTime.now().plusDays(10))
                .build();
        when(quoteRepository.findByPropertyIdAndExpiresAtAfter(eq(property.getId()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existingQuote));

        QuoteResponse response = quoteService.getQuote(SLUG, quoteRequest);

        assertThat(response.price()[0]).isEqualTo(200);
        assertThat(response.price()[1]).isEqualTo(300);
        verify(quoteRepository, never()).save(any());
    }

    @Test
    void givenPropertyInDb_withActiveQuoteForDifferentCustomer_whenGetQuote_thenSavesNewQuoteForCurrentCustomer() {
        Customer otherCustomer = Customer.builder().id(UUID.randomUUID()).email("other@example.com").build();

        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));
        when(companyPricingRepository.findByCompanyId(company.getId())).thenReturn(Optional.of(companyPricing));
        when(customerService.findOrCreate(quoteRequest)).thenReturn(customer);
        when(propertyRepository.existsByFullAddress(FORMATTED_ADDRESS)).thenReturn(true);
        when(propertyRepository.findByFullAddress(FORMATTED_ADDRESS)).thenReturn(Optional.of(property));
        when(propertyService.getDataFromExistingProperty(property)).thenReturn(
                PropertyData.builder().sqft(2000).stories(2).yearBuilt(2000).build()
        );

        Quote existingQuote = Quote.builder()
                .customer(otherCustomer)
                .property(property)
                .priceLow(200)
                .priceHigh(300)
                .expiresAt(LocalDateTime.now().plusDays(10))
                .build();
        when(quoteRepository.findByPropertyIdAndExpiresAtAfter(eq(property.getId()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existingQuote));

        QuoteResponse response = quoteService.getQuote(SLUG, quoteRequest);

        verify(quoteRepository, times(1)).save(argThat(q -> q.getCustomer().equals(customer)));
        assertThat(response.price()[0]).isEqualTo(200);
        assertThat(response.price()[1]).isEqualTo(300);
    }

    @Test
    void givenPropertyInDb_withExpiredQuote_whenGetQuote_thenRepricesAndSavesNewQuote() {
        PropertyData existingData = PropertyData.builder().sqft(2000).stories(2).yearBuilt(2000).build();

        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));
        when(companyPricingRepository.findByCompanyId(company.getId())).thenReturn(Optional.of(companyPricing));
        when(customerService.findOrCreate(quoteRequest)).thenReturn(customer);
        when(propertyRepository.existsByFullAddress(FORMATTED_ADDRESS)).thenReturn(true);
        when(propertyRepository.findByFullAddress(FORMATTED_ADDRESS)).thenReturn(Optional.of(property));
        when(propertyService.getDataFromExistingProperty(property)).thenReturn(existingData);
        when(quoteRepository.findByPropertyIdAndExpiresAtAfter(eq(property.getId()), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(pricingService.getPrice(any(), eq(existingData), anyString())).thenReturn(new Integer[]{250, 350});

        QuoteResponse response = quoteService.getQuote(SLUG, quoteRequest);

        verify(pricingService, times(1)).getPrice(any(), eq(existingData), anyString());
        verify(quoteRepository, times(1)).save(argThat(q -> q.getProperty().equals(property)));
        assertThat(response.price()[0]).isEqualTo(250);
        assertThat(response.price()[1]).isEqualTo(350);
    }

    @Test
    void givenNewProperty_withCompleteData_whenGetQuote_thenSavesPropertyAndQuoteAndReturnsPrice() {
        PropertyData data = PropertyData.builder().sqft(2000).stories(2).yearBuilt(2000).build();
        LookupResult lookupResult = new LookupResult(data, "ok");

        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));
        when(companyPricingRepository.findByCompanyId(company.getId())).thenReturn(Optional.of(companyPricing));
        when(customerService.findOrCreate(quoteRequest)).thenReturn(customer);
        when(propertyRepository.existsByFullAddress(FORMATTED_ADDRESS)).thenReturn(false);
        when(lookupProcess.startProcess(FORMATTED_ADDRESS)).thenReturn(lookupResult);
        when(pricingService.getPrice(any(), eq(data), anyString())).thenReturn(new Integer[]{300, 400});
        when(propertyRepository.findByFullAddress(FORMATTED_ADDRESS)).thenReturn(Optional.empty());
        when(propertyService.createProperty(anyString(), any(), any())).thenReturn(property);

        QuoteResponse response = quoteService.getQuote(SLUG, quoteRequest);

        verify(propertyRepository, times(1)).save(property);
        verify(quoteRepository, times(1)).save(any(Quote.class));
        assertThat(response.price()[0]).isEqualTo(300);
        assertThat(response.price()[1]).isEqualTo(400);
        assertThat(response.customerId()).isNull();
    }

    @Test
    void givenNewProperty_withMissingData_whenGetQuote_thenReturnsNullPriceAndCustomerId() {
        // Lookup came back missing sqft and stories — signals client to submit an amended quote
        PropertyData incompleteData = PropertyData.builder().yearBuilt(2000).build();
        LookupResult lookupResult = new LookupResult(incompleteData, "missing fields");

        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));
        when(companyPricingRepository.findByCompanyId(company.getId())).thenReturn(Optional.of(companyPricing));
        when(customerService.findOrCreate(quoteRequest)).thenReturn(customer);
        when(propertyRepository.existsByFullAddress(FORMATTED_ADDRESS)).thenReturn(false);
        when(lookupProcess.startProcess(FORMATTED_ADDRESS)).thenReturn(lookupResult);

        QuoteResponse response = quoteService.getQuote(SLUG, quoteRequest);

        assertThat(response.price()).isNull();
        assertThat(response.customerId()).isEqualTo(customer.getId());
        verify(propertyRepository, never()).save(any());
        verify(quoteRepository, never()).save(any());
    }

    // amendQuote

    @Test
    void givenUnknownCustomer_whenAmendQuote_thenThrowsCustomerNotFoundException() {
        UUID unknownCustomerId = UUID.randomUUID();
        AmendedQuoteRequest request = new AmendedQuoteRequest(
                unknownCustomerId,
                PropertyData.builder().sqft(2000).stories(2).yearBuilt(2000).build(),
                new AddressInfo("123 Main St", "Charlotte", "NC", "28202"),
                "never"
        );

        when(customerRepository.findById(unknownCustomerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.amendQuote(SLUG, request))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void givenUnknownSlug_whenAmendQuote_thenThrowsCompanyNotFoundException() {
        AmendedQuoteRequest request = new AmendedQuoteRequest(
                customer.getId(),
                PropertyData.builder().sqft(2000).stories(2).yearBuilt(2000).build(),
                new AddressInfo("123 Main St", "Charlotte", "NC", "28202"),
                "never"
        );

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.amendQuote(SLUG, request))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void givenNoPricingConfigured_whenAmendQuote_thenThrowsPricingNotConfiguredException() {
        AmendedQuoteRequest request = new AmendedQuoteRequest(
                customer.getId(),
                PropertyData.builder().sqft(2000).stories(2).yearBuilt(2000).build(),
                new AddressInfo("123 Main St", "Charlotte", "NC", "28202"),
                "never"
        );

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));
        when(companyPricingRepository.findByCompanyId(company.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.amendQuote(SLUG, request))
                .isInstanceOf(PricingNotConfiguredException.class);
    }

    @Test
    void givenExistingProperty_whenAmendQuote_thenUsesExistingPropertyAndReturnsPrice() {
        PropertyData data = PropertyData.builder().sqft(2000).stories(2).yearBuilt(2000).build();
        AmendedQuoteRequest request = new AmendedQuoteRequest(
                customer.getId(), data,
                new AddressInfo("123 Main St", "Charlotte", "NC", "28202"),
                "never"
        );

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));
        when(companyPricingRepository.findByCompanyId(company.getId())).thenReturn(Optional.of(companyPricing));
        when(propertyRepository.findByFullAddress(FORMATTED_ADDRESS)).thenReturn(Optional.of(property));
        when(pricingService.getPrice(any(), eq(data), anyString())).thenReturn(new Integer[]{400, 500});

        AmendedPriceResponse response = quoteService.amendQuote(SLUG, request);

        verify(propertyService, never()).createProperty(anyString(), any(), any());
        verify(propertyRepository, times(1)).save(property);
        verify(quoteRepository, times(1)).save(any(Quote.class));
        assertThat(response.price()[0]).isEqualTo(400);
        assertThat(response.price()[1]).isEqualTo(500);
    }

    @Test
    void givenNewProperty_whenAmendQuote_thenCreatesPropertyAndReturnsPrice() {
        PropertyData data = PropertyData.builder().sqft(2000).stories(2).yearBuilt(2000).build();
        AmendedQuoteRequest request = new AmendedQuoteRequest(
                customer.getId(), data,
                new AddressInfo("123 Main St", "Charlotte", "NC", "28202"),
                "never"
        );

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(companyRepository.findBySlug(SLUG)).thenReturn(Optional.of(company));
        when(companyPricingRepository.findByCompanyId(company.getId())).thenReturn(Optional.of(companyPricing));
        when(propertyRepository.findByFullAddress(FORMATTED_ADDRESS)).thenReturn(Optional.empty());
        when(propertyService.createProperty(eq(FORMATTED_ADDRESS), any(AddressInfo.class), eq(data))).thenReturn(property);
        when(pricingService.getPrice(any(), eq(data), anyString())).thenReturn(new Integer[]{400, 500});

        AmendedPriceResponse response = quoteService.amendQuote(SLUG, request);

        verify(propertyService, times(1)).createProperty(eq(FORMATTED_ADDRESS), any(AddressInfo.class), eq(data));
        verify(propertyRepository, times(1)).save(property);
        verify(quoteRepository, times(1)).save(any(Quote.class));
        assertThat(response.price()[0]).isEqualTo(400);
        assertThat(response.price()[1]).isEqualTo(500);
    }
}
