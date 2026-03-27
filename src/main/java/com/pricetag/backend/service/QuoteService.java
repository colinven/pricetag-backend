package com.pricetag.backend.service;

import com.pricetag.backend.dto.*;
import com.pricetag.backend.dto.request.AmendedQuoteRequest;
import com.pricetag.backend.dto.request.QuoteRequest;
import com.pricetag.backend.dto.response.AmendedPriceResponse;
import com.pricetag.backend.dto.response.QuoteResponse;
import com.pricetag.backend.process.LookupProcess;
import com.pricetag.backend.process.LookupResult;
import com.pricetag.backend.util.AddressFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final AddressFormatter addressFormatter;
    private final LookupProcess lookupProcess;
    private final PricingService pricingService;

    public QuoteResponse getQuote(QuoteRequest request) {
        String formattedAddress = addressFormatter.formatAddress(new AddressInfo(request.street(), request.city(), request.state(), request.zip()));
        LookupResult lookupResult = lookupProcess.startProcess(formattedAddress);
        if (lookupResult.data() != null) {
            Integer[] priceRange = pricingService.getPrice(lookupResult.data(), request.lastWash());
            return new QuoteResponse(lookupResult, priceRange, formattedAddress);
        } else return new QuoteResponse(lookupResult, null, formattedAddress);

    }

    public AmendedPriceResponse amendQuote(AmendedQuoteRequest request) {
        return new AmendedPriceResponse(pricingService.getPrice(request.data(), request.lastWash()));
    }
}
