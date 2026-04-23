package com.pricetag.backend.controller;

import com.pricetag.backend.dto.request.FinalizeQuoteRequest;
import com.pricetag.backend.dto.request.UpdateQuoteStatusRequest;
import com.pricetag.backend.dto.response.*;
import com.pricetag.backend.entity.Quote;
import com.pricetag.backend.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
public class DashboardController extends BaseController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(HttpServletRequest request) {
        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(dashboardService.getDashboardSummary(companyId));
    }

    @GetMapping("/dashboard/quotes-to-review")
    public ResponseEntity<QuotesResponse> getPendingQuotesToReview(HttpServletRequest request) {
        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(dashboardService.getPendingQuotes(companyId));
    }

    @GetMapping("/dashboard/quotes")
    public ResponseEntity<Page<QuoteSummary>> getQuotes(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue =  "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "") Set<Quote.Status> statuses) {

        UUID companyId = extractCompanyId(request);
        Set<Quote.Status> effectiveStatuses = (statuses == null || statuses.isEmpty())
                ? EnumSet.allOf(Quote.Status.class)
                : statuses;
        return ResponseEntity.ok(dashboardService.getQuotes(companyId, page, size, sortBy, direction, effectiveStatuses));
    }

    @GetMapping("/dashboard/quotes/{quoteId}")
    public ResponseEntity<QuoteDetails> getQuoteDetails(
            HttpServletRequest request,
            @PathVariable UUID quoteId) {

        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(dashboardService.getQuoteDetailsById(companyId, quoteId));
    }

    @PatchMapping("/dashboard/quotes/{quoteId}/finalize")
    public ResponseEntity<FinalizedQuoteResponse> finalizeQuote(
            HttpServletRequest request,
            @PathVariable("quoteId") UUID quoteId,
            @RequestBody @Valid FinalizeQuoteRequest finalizeQuoteRequest) {

        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(dashboardService.finalizeQuote(companyId, quoteId, finalizeQuoteRequest.finalPrice()));
    }

    @PatchMapping("/dashboard/quotes/{quoteId}/status")
    public ResponseEntity<FinalizedQuoteResponse> updateQuoteStatus(
            HttpServletRequest request,
            @PathVariable("quoteId") UUID quoteId,
            @RequestBody @Valid UpdateQuoteStatusRequest updateQuoteStatusRequest) {

        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(dashboardService.manuallyChangeQuoteStatus(companyId, quoteId, updateQuoteStatusRequest.status()));
    }

    @GetMapping("/dashboard/customers")
    public ResponseEntity<Page<CustomerSummary>> getCustomers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(dashboardService.getCustomers(companyId, page, size, sortBy, direction));
    }

    @GetMapping("/dashboard/customers/{customerId}")
    public ResponseEntity<CustomerDetails> getCustomerDetails(
            HttpServletRequest request,
            @PathVariable UUID customerId) {

        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(dashboardService.getCustomerDetailsAndQuotesById(companyId, customerId));
    }
}
