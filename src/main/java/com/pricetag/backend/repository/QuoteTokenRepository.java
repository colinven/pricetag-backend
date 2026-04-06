package com.pricetag.backend.repository;

import com.pricetag.backend.entity.QuoteToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuoteTokenRepository extends JpaRepository<QuoteToken, UUID> {

    Optional<QuoteToken> findByQuoteId(UUID quoteId);



}
