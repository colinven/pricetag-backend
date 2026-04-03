package com.pricetag.backend.controller;

import com.pricetag.backend.dto.request.FinalizeQuoteRequest;
import com.pricetag.backend.dto.response.FinalizedQuoteResponse;
import com.pricetag.backend.dto.response.QuoteSummary;
import com.pricetag.backend.dto.response.DashboardSummaryResponse;
import com.pricetag.backend.dto.response.QuotesResponse;
import com.pricetag.backend.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(defaultValue = "desc") String direction) {

        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(dashboardService.getQuotes(companyId, page, size, sortBy, direction));
    }

    @PatchMapping("/dashboard/quotes/{quoteId}/finalize")
    public ResponseEntity<FinalizedQuoteResponse> finalizeQuote(
            HttpServletRequest request,
            @PathVariable("quoteId") UUID quoteId,
            @RequestBody FinalizeQuoteRequest finalizeQuoteRequest) {

        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(dashboardService.finalizeQuote(companyId, quoteId, finalizeQuoteRequest.finalPrice()));
    }
}
