package com.pricetag.backend.repository;

import com.pricetag.backend.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    Optional<Property> findByFullAddress(String fullAddress);

    boolean existsByFullAddress(String fullAddress);
}
