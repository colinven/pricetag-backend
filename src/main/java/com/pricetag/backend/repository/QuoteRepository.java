package com.pricetag.backend.repository;

import com.pricetag.backend.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, UUID> {
}
