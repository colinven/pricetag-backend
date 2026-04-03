package com.pricetag.backend.repository;

import com.pricetag.backend.dto.response.QuoteSummary;
import com.pricetag.backend.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, UUID> {

    Optional<Quote> findFirstByPropertyIdAndExpiresAtAfter(UUID propertyId, LocalDateTime now);

    boolean existsByPropertyIdAndCustomerIdAndExpiresAtAfter(UUID propertyId, UUID customerId, LocalDateTime now);

    int countByCompanyId(UUID companyId);

    int countByCompanyIdAndCreatedAtAfter(UUID companyId, LocalDateTime createdAt);

    int countByCompanyIdAndStatus(UUID companyId, Quote.Status status);

    int countByCompanyIdAndStatusIn(UUID companyId, List<Quote.Status> statuses);

    @Query("SELECT q.finalPrice FROM Quote q WHERE q.company.id = :companyId AND q.status IN :statuses")
    List<Integer> findFinalPricesByCompanyIdAndStatusIn(UUID companyId, List<Quote.Status> statuses);

    @Query("""
            SELECT new com.pricetag.backend.dto.response.QuoteSummary(
                q.id, q.status, q.customer.firstName, q.customer.lastName,
                q.property.fullAddress, q.priceLow, q.priceHigh, q.finalPrice, q.createdAt)
            FROM Quote q
            WHERE q.company.id = :companyId AND q.status = :status
            ORDER BY q.createdAt ASC
            """
    )
    List<QuoteSummary> findByCompanyIdAndStatusOrderByCreatedAtAsc(UUID companyId, Quote.Status status);

    @Query(value = """
                SELECT new com.pricetag.backend.dto.response.QuoteSummary(
                q.id, q.status, q.customer.firstName, q.customer.lastName,
                q.property.fullAddress, q.priceLow, q.priceHigh, q.finalPrice, q.createdAt)
                FROM Quote q
                WHERE q.company.id = :companyId
            """,
            countQuery = "SELECT COUNT(q) FROM Quote q WHERE q.company.id = :companyId"
    )
    Page<QuoteSummary> findByCompanyId(UUID companyId, Pageable pageable);
}
