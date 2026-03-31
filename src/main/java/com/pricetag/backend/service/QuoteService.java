package com.pricetag.backend.service;

import com.pricetag.backend.dto.*;
import com.pricetag.backend.dto.request.AmendedQuoteRequest;
import com.pricetag.backend.dto.request.QuoteRequest;
import com.pricetag.backend.dto.response.AmendedPriceResponse;
import com.pricetag.backend.dto.response.QuoteResponse;
import com.pricetag.backend.entity.*;
import com.pricetag.backend.exception.CompanyNotFoundException;
import com.pricetag.backend.exception.GeocodingException;
import com.pricetag.backend.exception.OutOfServiceAreaException;
import com.pricetag.backend.exception.PricingNotConfiguredException;
import com.pricetag.backend.process.LookupProcess;
import com.pricetag.backend.process.LookupResult;
import com.pricetag.backend.repository.*;
import com.pricetag.backend.util.AddressFormatter;
import com.pricetag.backend.util.GeoUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final LookupProcess lookupProcess;
    private final CustomerService customerService;
    private final PropertyService propertyService;
    private final PricingService pricingService;
    private final GoogleMapsService googleMapsService;

    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final CompanyPricingRepository companyPricingRepository;
    private final PropertyRepository propertyRepository;
    private final QuoteRepository quoteRepository;

    @Transactional
    public QuoteResponse getQuote(String slug, QuoteRequest request) {

        Company company = companyRepository.findBySlug(slug)
                .orElseThrow(() -> new CompanyNotFoundException(slug));

        CompanyPricing companyPricing = companyPricingRepository.findByCompanyId(company.getId())
                .orElseThrow(PricingNotConfiguredException::new);

        Customer customer = customerService.findOrCreate(request);
        customerRepository.save(customer);

        AddressInfo addressInfo = new AddressInfo(request.street(), request.city(), request.state(), request.zip());
        String formattedAddress = AddressFormatter.formatAddress(addressInfo);

        if (company.hasServiceAreaConfigured()) {
            try {
                LatLng propertyCoordinates = googleMapsService.geocode(formattedAddress);
                if (!GeoUtils.isInServiceArea(company, propertyCoordinates)) throw new OutOfServiceAreaException();
            }
            catch (GeocodingException e) {
                // google geocoding failed - bypass service area check and continue
                // TODO find a better solution to failed google geocode calls
            }
        }
        // TODO if property already exists in DB, pull that record, otherwise lookup
        // after checking if property exists, check if a quote existsByPropertyId && !isExpired. if true, return that quote.
        LookupResult lookupResult = lookupProcess.startProcess(formattedAddress);

        if (lookupResult.data() != null) {
            Integer[] priceRange = pricingService.getPrice(companyPricing, lookupResult.data(), request.lastWash());

            Property property = propertyService.createProperty(formattedAddress, request, lookupResult);
            propertyRepository.save(property);

            Quote quoteEntity = Quote.builder()
                    .company(company)
                    .customer(customer)
                    .property(property)
                    .priceLow(priceRange[0])
                    .priceHigh(priceRange[1])
                    .status(Quote.Status.PENDING)
                    .expiresAt(LocalDateTime.now().plusDays(companyPricing.getQuoteExpiryDays()))
                    .build();
            quoteRepository.save(quoteEntity);

            return QuoteResponse.builder()
                    .lookupResult(lookupResult)
                    .price(priceRange)
                    .address(formattedAddress)
                    .build();

        } else return QuoteResponse.builder()
                .lookupResult(lookupResult)
                .price(null)
                .address(formattedAddress)
                .customerId(customer.getId())
                .build();

    }

    // TODO need to figure out how we will receive back all necessary data to create Property record in db
    // Currently, amended Quote records along with associated Property records do not get added to DB.
    public AmendedPriceResponse amendQuote(String slug, AmendedQuoteRequest request) {

        Company company = companyRepository.findBySlug(slug)
                .orElseThrow(() -> new CompanyNotFoundException(slug));
        CompanyPricing companyPricing = companyPricingRepository.findByCompanyId(company.getId())
                .orElseThrow(PricingNotConfiguredException::new);


        return new AmendedPriceResponse(pricingService.getPrice(companyPricing, request.data(), request.lastWash()));
    }
}
