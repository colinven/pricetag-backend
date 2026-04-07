package com.pricetag.backend.controller;

import com.pricetag.backend.dto.request.CustomerQuoteDecision;
import com.pricetag.backend.dto.response.*;
import com.pricetag.backend.dto.request.AmendedQuoteRequest;
import com.pricetag.backend.dto.request.QuoteRequest;
import com.pricetag.backend.entity.Quote;
import com.pricetag.backend.repository.CompanyRepository;
import com.pricetag.backend.service.CompanyService;
import com.pricetag.backend.service.QuoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Validated
public class QuoteController {

    private final QuoteService quoteService;
    private final CompanyService companyService;

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

    @GetMapping("/{slug}/company-info")
    public ResponseEntity<CompanySummary> getCompanyInfoBySlug(
            @PathVariable String slug) {
        return ResponseEntity.ok(companyService.getCompanySummaryFromSlug(slug));
    }

    @GetMapping("/quotes/{quoteId}")
    public ResponseEntity<QuoteAndCompanyDetails> viewFinalizedQuote(
            @PathVariable UUID quoteId,
            @RequestParam(required = false) String token) {
        return ResponseEntity.ok(quoteService.viewFinalizedQuote(quoteId, token));
    }

    @PatchMapping("/quotes/{quoteId}")
    public ResponseEntity<FinalizedQuoteResponse> acceptOrDeclineQuote(
            @PathVariable UUID quoteId,
            @RequestParam String token,
            @RequestBody @Valid CustomerQuoteDecision decision) {
        return ResponseEntity.ok(quoteService.changeQuoteStatus(quoteId, token, decision.status()));
    }
}
