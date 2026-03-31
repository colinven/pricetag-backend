package com.pricetag.backend.controller;

import com.pricetag.backend.dto.response.AmendedPriceResponse;
import com.pricetag.backend.dto.request.AmendedQuoteRequest;
import com.pricetag.backend.dto.request.QuoteRequest;
import com.pricetag.backend.dto.response.QuoteResponse;
import com.pricetag.backend.repository.CompanyRepository;
import com.pricetag.backend.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;
    private final CompanyRepository companyRepository;

    @PostMapping("/{slug}/quote-request")
    public ResponseEntity<QuoteResponse> getQuote(
            @PathVariable String slug,
            @RequestBody @Valid QuoteRequest quoteRequest) {
        QuoteResponse quoteResponse = quoteService.getQuote(slug, quoteRequest);
        return ResponseEntity.ok(quoteResponse);
    }

    @PostMapping("/{slug}/quote-request/amend")
    public ResponseEntity<AmendedPriceResponse> amendQuote(
            @PathVariable String slug,
            @RequestBody @Valid AmendedQuoteRequest amendedQuoteRequest) {
        AmendedPriceResponse amendedPriceResponse = quoteService.amendQuote(slug, amendedQuoteRequest);
        return ResponseEntity.ok(amendedPriceResponse);
    }

    @GetMapping("/{slug}/validate")
    public ResponseEntity<Boolean> validateSlug(
            @PathVariable String slug) {
        return ResponseEntity.ok(companyRepository.existsBySlug(slug));
    }

}
