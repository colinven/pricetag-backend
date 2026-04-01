package com.pricetag.backend.repository;

import com.pricetag.backend.entity.Company;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    boolean existsBySlug(String slug);

    boolean existsByEmail(String email);

    boolean existsById(@NonNull UUID id);

    Optional<Company> findBySlug(String slug);
}
