package com.pricetag.backend.service;

import com.pricetag.backend.dto.response.*;
import com.pricetag.backend.entity.Customer;
import com.pricetag.backend.entity.Property;
import com.pricetag.backend.entity.Quote;
import com.pricetag.backend.exception.CompanyNotFoundException;
import com.pricetag.backend.exception.InvalidQuoteStatusException;
import com.pricetag.backend.exception.QuoteNotFoundException;
import com.pricetag.backend.repository.CompanyRepository;
import com.pricetag.backend.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CompanyRepository companyRepository;
    private final QuoteRepository quoteRepository;

    public DashboardSummaryResponse getDashboardSummary (UUID companyId) {
        if (!companyRepository.existsById(companyId)) throw new CompanyNotFoundException(companyId);

        int totalNumOfQuotes = quoteRepository.countByCompanyId(companyId);
        int numOfQuotesThirtyDays = quoteRepository.countByCompanyIdAndCreatedAtAfter(companyId, LocalDateTime.now().minusDays(30));
        int numOfQuotesToReview = quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.PENDING);

        int totalReviewedQuotes =  quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.REVIEWED);
        int totalAcceptedQuotes =  quoteRepository.countByCompanyIdAndStatus(companyId, Quote.Status.ACCEPTED);
        double conversionRate = totalReviewedQuotes > 0 ? (double)totalAcceptedQuotes/totalReviewedQuotes : 0.0;

        List<Integer> finalPrices = quoteRepository.findFinalPricesByCompanyIdAndStatusIn(
                companyId,
                List.of(Quote.Status.REVIEWED, Quote.Status.ACCEPTED,  Quote.Status.DECLINED)
                );
        OptionalDouble averageFinalPrice = finalPrices.stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();

        return DashboardSummaryResponse.builder()
                .totalNumOfQuotes(totalNumOfQuotes)
                .numOfQuotesThirtyDays(numOfQuotesThirtyDays)
                .numOfQuotesToReview(numOfQuotesToReview)
                .conversionRate(conversionRate)
                .averageFinalPrice(averageFinalPrice.orElse(0.0))
                .build();
    }

    public QuotesResponse getPendingQuotes (UUID companyId) {
        if (!companyRepository.existsById(companyId)) throw new CompanyNotFoundException(companyId);
        List<QuoteSummary> pendingQuotes = quoteRepository.findByCompanyIdAndStatusOrderByCreatedAtAsc(companyId, Quote.Status.PENDING);
        return new  QuotesResponse(pendingQuotes);
    }

    public Page<QuoteSummary> getQuotes(UUID companyId, int page, int size, String sortBy, String sortDirection) {
        if (!companyRepository.existsById(companyId)) throw new CompanyNotFoundException(companyId);
        Sort sort = sortDirection.equals("desc")
                ? Sort.by(mapSortBy(sortBy)).descending()
                : Sort.by(mapSortBy(sortBy)).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return quoteRepository.findByCompanyId(companyId, pageable);
    }

    private String mapSortBy(String sortBy) {
        return switch (sortBy) {
            case "customerFirstName" -> "customer.firstName";
            case "customerLastName" -> "customer.lastName";
            case "propertyAddress" -> "property.fullAddress";
            default -> "createdAt";
        };
    }

    public QuoteDetails getQuoteDetailsById(UUID companyId, UUID quoteId) {
        if (!companyRepository.existsById(companyId)) throw new CompanyNotFoundException(companyId);
        Quote quote = quoteRepository.findById(quoteId).orElseThrow(() -> new  QuoteNotFoundException(quoteId));
        Customer customer = quote.getCustomer();
        Property property = quote.getProperty();

        return QuoteDetails.builder()
                .id(quoteId)
                .customerFirstName(customer.getFirstName()).customerLastName(customer.getLastName())
                .customerEmail(customer.getEmail()).customerPhone(customer.getPhone())
                .propertyAddress(property.getFullAddress())
                .propertySqft(property.getSqft()).propertyStories(property.getNumberOfStories())
                .propertyYearBuilt(property.getYearBuilt()).propertyGarageSize(property.getGarageSizeCars())
                .propertyType(property.getPropertyType())
                .priceLow(quote.getPriceLow()).priceHigh(quote.getPriceHigh())
                .finalPrice(quote.getFinalPrice())
                .status(quote.getStatus())
                .createdAt(quote.getCreatedAt())
                .reviewedAt(quote.getReviewedAt())
                .acceptedAt(quote.getAcceptedAt())
                .declinedAt(quote.getDeclinedAt())
                .expiresAt(quote.getExpiresAt())
                .build();
    }

    public FinalizedQuoteResponse finalizeQuote(UUID companyId, UUID quoteId, Integer finalPrice) {
        if (!companyRepository.existsById(companyId)) throw new CompanyNotFoundException(companyId);
        Quote quote = quoteRepository.findById(quoteId).orElseThrow(() -> new QuoteNotFoundException(quoteId));
        quote.setFinalPrice(finalPrice);
        quote.setStatus(Quote.Status.REVIEWED);
        quote.setReviewedAt(LocalDateTime.now());
        quoteRepository.save(quote);
        return FinalizedQuoteResponse.builder()
                .quoteId(quoteId)
                .finalPrice(finalPrice)
                .status(Quote.Status.REVIEWED)
                .build();
    }

    public FinalizedQuoteResponse manuallyChangeQuoteStatus(UUID companyId, UUID quoteId, Quote.Status status) {
        if (status != Quote.Status.DECLINED && status != Quote.Status.ACCEPTED) {
            throw new InvalidQuoteStatusException("Status must be DECLINED or ACCEPTED");
        }
        if (!companyRepository.existsById(companyId)) throw new CompanyNotFoundException(companyId);
        Quote quote = quoteRepository.findById(quoteId).orElseThrow(() -> new QuoteNotFoundException(quoteId));
        quote.setStatus(status);
        if (status == Quote.Status.ACCEPTED) {
            quote.setAcceptedAt(LocalDateTime.now());
            quote.setDeclinedAt(null);
        }
        if (status == Quote.Status.DECLINED) {
            quote.setDeclinedAt(LocalDateTime.now());
            quote.setAcceptedAt(null);
        }
        quoteRepository.save(quote);
        return FinalizedQuoteResponse.builder()
                .quoteId(quoteId)
                .finalPrice(quote.getFinalPrice())
                .status(quote.getStatus())
                .build();
    }

}
