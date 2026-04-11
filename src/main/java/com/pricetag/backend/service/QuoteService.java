package com.pricetag.backend.service;

import com.pricetag.backend.dto.*;
import com.pricetag.backend.dto.request.AmendedQuoteRequest;
import com.pricetag.backend.dto.request.QuoteRequest;
import com.pricetag.backend.dto.response.*;
import com.pricetag.backend.entity.*;
import com.pricetag.backend.exception.*;
import com.pricetag.backend.process.LookupProcess;
import com.pricetag.backend.process.LookupResult;
import com.pricetag.backend.repository.*;
import com.pricetag.backend.util.Formatter;
import com.pricetag.backend.util.GeoUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final LookupProcess lookupProcess;
    private final CustomerService customerService;
    private final PropertyService propertyService;
    private final PricingService pricingService;
    private final QuoteTokenService quoteTokenService;
    private final GeoUtils geoUtils;

    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final CompanyPricingRepository companyPricingRepository;
    private final PropertyRepository propertyRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteTokenRepository quoteTokenRepository;

    @Transactional
    public QuoteResponse getQuote(String slug, QuoteRequest request) {

        Company company = companyRepository.findBySlug(slug).orElseThrow(() -> new CompanyNotFoundException(slug));
        CompanyPricing companyPricing = companyPricingRepository.findByCompanyId(company.getId()).orElseThrow(PricingNotConfiguredException::new);
        Customer customer = customerService.findOrCreate(request, company);
        AddressInfo addressInfo = new AddressInfo(request.street(), request.city(), request.state(), request.zip());
        String formattedAddress = Formatter.formatAddress(addressInfo);


        // Reject requests for properties out of company service area
        if (company.hasServiceAreaConfigured() && !geoUtils.propertyIsInServiceArea(company, formattedAddress)) throw new OutOfServiceAreaException();

        customerRepository.save(customer);

        // This block executes when the requested property already exists in the db by address (prevents unnecessary property scrape):
        if (propertyRepository.existsByFullAddress(formattedAddress)) {
            Property existingProperty = propertyRepository.findByFullAddress(formattedAddress).orElseThrow();
            PropertyData existingPropertyData = propertyService.getDataFromExistingProperty(existingProperty);
            LookupResult existingResult = new LookupResult(existingPropertyData, "Property data successfully retrieved from database.");

            // Query db to see if there is any existing active quote for the requested property:
            Quote existingQuote = quoteRepository.findFirstByPropertyIdAndExpiresAtAfter(existingProperty.getId(), LocalDateTime.now())
                    .orElse(null);

            boolean customerAlreadyHasActiveQuote = quoteRepository.existsByPropertyIdAndCustomerIdAndCompanyIdAndExpiresAtAfter(
                    existingProperty.getId(), customer.getId(), company.getId(), LocalDateTime.now());

            // If active quote exists for property but this customer doesn't have one yet, insert a new one
            if (existingQuote != null && !customerAlreadyHasActiveQuote) {
                Quote quote = Quote.builder()
                        .company(company)
                        .customer(customer)
                        .property(existingProperty)
                        .priceLow(existingQuote.getPriceLow())
                        .priceHigh(existingQuote.getPriceHigh())
                        .status(Quote.Status.PENDING)
                        .expiresAt(LocalDateTime.now().plusDays(companyPricing.getQuoteExpiryDays()))
                        .build();
                quoteRepository.save(quote);
            }
            // Set price identical to existing active quote, otherwise re-price if quote is expired
            Integer[] price = existingQuote != null ?
                    new Integer[]{existingQuote.getPriceLow(), existingQuote.getPriceHigh()} :
                    pricingService.getPrice(companyPricing, existingPropertyData, request.lastWash());

            // If no active quote was found for this property, insert a new one
            if (existingQuote == null) {
                Quote quote = Quote.builder()
                        .company(company)
                        .customer(customer)
                        .property(existingProperty)
                        .priceLow(price[0])
                        .priceHigh(price[1])
                        .status(Quote.Status.PENDING)
                        .expiresAt(LocalDateTime.now().plusDays(companyPricing.getQuoteExpiryDays()))
                        .build();
                quoteRepository.save(quote);
            }

            return QuoteResponse.builder()
                    .lookupResult(existingResult)
                    .price(price)
                    .address(existingProperty.getFullAddress())
                    .build();
        }

        // Only scrape property if property data is not already in db
        LookupResult lookupResult = lookupProcess.startProcess(formattedAddress);

        boolean resultIsMissingData = lookupResult.data() == null ||
                lookupResult.data().sqft() == null ||
                lookupResult.data().stories() == null;

        // If property data came back with missing fields, set null price in QuoteResponse (signals client that quote needs amendment)
        Integer[] newPriceRange = resultIsMissingData ? null :
                pricingService.getPrice(companyPricing, lookupResult.data(), request.lastWash());

        // If property data came back whole, generate quote, insert Property and Quote entities into db, return complete QuoteRequest
        if (!resultIsMissingData) {
            Property property = propertyRepository.findByFullAddress(formattedAddress)
                    .orElseGet(() -> propertyService.createProperty(formattedAddress, addressInfo, lookupResult.data()));

            Quote quoteEntity = Quote.builder()
                    .company(company)
                    .customer(customer)
                    .property(property)
                    .priceLow(newPriceRange[0])
                    .priceHigh(newPriceRange[1])
                    .status(Quote.Status.PENDING)
                    .expiresAt(LocalDateTime.now().plusDays(companyPricing.getQuoteExpiryDays()))
                    .build();

            propertyRepository.save(property);
            quoteRepository.save(quoteEntity);

        }
        return QuoteResponse.builder()
                .lookupResult(lookupResult)
                .price(newPriceRange)
                .address(formattedAddress)
                .customerId(resultIsMissingData ? customer.getId() : null)
                .build();

    }

    @Transactional
    public AmendedPriceResponse amendQuote(String slug, AmendedQuoteRequest request) {

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.customerId()));
        Company company = companyRepository.findBySlug(slug)
                .orElseThrow(() -> new CompanyNotFoundException(slug));
        CompanyPricing companyPricing = companyPricingRepository.findByCompanyId(company.getId())
                .orElseThrow(PricingNotConfiguredException::new);
        String formattedAddress = Formatter.formatAddress(request.addressInfo());
        Property  property = propertyRepository.findByFullAddress(formattedAddress)
                .orElseGet(() -> propertyService.createProperty(
                        formattedAddress,
                        request.addressInfo(),
                        request.data())
                );

        Integer[] priceRange = pricingService.getPrice(companyPricing, request.data(), request.lastWash());

        Quote quoteEntity = Quote.builder()
                .company(company)
                .customer(customer)
                .property(property)
                .priceLow(priceRange[0])
                .priceHigh(priceRange[1])
                .status(Quote.Status.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(companyPricing.getQuoteExpiryDays()))
                .build();

        propertyRepository.save(property);
        quoteRepository.save(quoteEntity);

        return new AmendedPriceResponse(priceRange);
    }

    public QuoteAndCompanyDetails viewFinalizedQuote(UUID quoteId, String quoteToken) {

        quoteTokenService.validateToken(quoteId, quoteToken);

        Quote quote = quoteRepository.findById(quoteId).orElseThrow(() -> new QuoteNotFoundException(quoteId));
        Customer customer = quote.getCustomer();
        Property property = quote.getProperty();
        Company company = quote.getCompany();
        return QuoteAndCompanyDetails.builder()
                .id(quote.getId())
                .companyName(company.getName())
                .companyEmail(company.getEmail())
                .companyPhone(company.getPhone())
                .customerFirstName(customer.getFirstName()).customerLastName(customer.getLastName())
                .customerEmail(customer.getEmail()).customerPhone(customer.getPhone())
                .propertyAddress(property.getFullAddress())
                .propertySqft(property.getSqft())
                .propertyStories(property.getNumberOfStories())
                .propertyYearBuilt(property.getYearBuilt())
                .propertyGarageSize(property.getGarageSizeCars())
                .propertyType(property.getPropertyType())
                .priceLow(quote.getPriceLow()).priceHigh(quote.getPriceHigh())
                .finalPrice(quote.getFinalPrice())
                .status(quote.getStatus())
                .createdAt(quote.getCreatedAt())
                .reviewedAt(quote.getReviewedAt())
                .expiresAt(quote.getExpiresAt())
                .build();

    }

    public FinalizedQuoteResponse changeQuoteStatus(UUID quoteId, String quoteToken, Quote.Status status) {

        if (status != Quote.Status.ACCEPTED && status != Quote.Status.DECLINED) {
            throw new InvalidQuoteStatusException("Updated status must be ACCEPTED or DECLINED.");
        }

        // validate token - if token has already been consumed, reject request (customer can only accept/decline once)
        QuoteToken savedToken = quoteTokenService.validateToken(quoteId, quoteToken);
        quoteTokenService.checkIfTokenConsumed(savedToken);

        Quote quote = quoteRepository.findById(quoteId).orElseThrow(() -> new QuoteNotFoundException(quoteId));

        if (status == Quote.Status.ACCEPTED) {
            quote.setAcceptedAt(LocalDateTime.now());
            quote.setDeclinedAt(null);
        } else {
            quote.setDeclinedAt(LocalDateTime.now());
            quote.setAcceptedAt(null);
        }

        // update quote status (ACCEPTED/DECLINED) and consume token
        quote.setStatus(status);
        quoteRepository.save(quote);
        quoteTokenService.consumeToken(savedToken);

        return FinalizedQuoteResponse.builder()
                .quoteId(quote.getId())
                .status(quote.getStatus())
                .finalPrice(quote.getFinalPrice())
                .build();
    }
}
