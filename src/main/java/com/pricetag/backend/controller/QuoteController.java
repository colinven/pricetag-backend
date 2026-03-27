package com.pricetag.backend.controller;

import com.pricetag.backend.dto.response.AmendedPriceResponse;
import com.pricetag.backend.dto.request.AmendedQuoteRequest;
import com.pricetag.backend.dto.request.QuoteRequest;
import com.pricetag.backend.dto.response.QuoteResponse;
import com.pricetag.backend.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<QuoteResponse> getQuote(@RequestBody @Valid QuoteRequest quoteRequest) {
        QuoteResponse quoteResponse = quoteService.getQuote(quoteRequest);
        return ResponseEntity.ok(quoteResponse);
    }

    @PostMapping("/amend")
    public ResponseEntity<AmendedPriceResponse> amendQuote(@RequestBody @Valid AmendedQuoteRequest amendedQuoteRequest) {
        AmendedPriceResponse amendedPriceResponse = quoteService.amendQuote(amendedQuoteRequest);
        return ResponseEntity.ok(amendedPriceResponse);
    }

}
