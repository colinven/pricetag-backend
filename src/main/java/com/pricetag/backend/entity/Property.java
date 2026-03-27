package com.pricetag.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "properties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "address_full", nullable = false, unique = true)
    private String fullAddress;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String zip;

    @Column(nullable = false)
    private int sqft;

    @Column(name = "stories", nullable = false)
    private int numberOfStories;

    @Column(name = "year_built", nullable = false)
    private int yearBuilt;

    @Column(name = "garage_size_cars")
    private Integer garageSizeCars;

    @Column(name = "property_type")
    private String propertyType;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
