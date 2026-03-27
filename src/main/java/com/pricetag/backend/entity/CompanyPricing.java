package com.pricetag.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "company_pricing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(name = "base_sqft_price")
    private Double baseSqftPrice;

    @Column(name = "story_multiplier")
    private Double storyMultiplier;

    @Column(name = "minimum_price")
    private Integer minimumPrice;

    @Column(name = "price_range_buffer")
    private Integer priceRangeBuffer;

    @Column(name = "quote_expiry_days", nullable = false)
    private int quoteExpiryDays;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
