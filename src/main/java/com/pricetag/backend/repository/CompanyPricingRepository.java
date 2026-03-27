package com.pricetag.backend.repository;

import com.pricetag.backend.entity.CompanyPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyPricingRepository extends JpaRepository<CompanyPricing, UUID> {
}
