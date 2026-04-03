package com.pricetag.backend.repository;

import com.pricetag.backend.dto.response.CustomerSummary;
import com.pricetag.backend.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmailAndCompanyId(String email, UUID companyId);

    Optional<Customer> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query(value = """
            SELECT new com.pricetag.backend.dto.response.CustomerSummary(
            c.id, c.firstName, c.lastName, c.createdAt)
            FROM Customer c
            WHERE c.company.id = :companyId
            """,
            countQuery = "SELECT COUNT(c) FROM Customer c WHERE c.company.id = :companyId"
    )
    Page<CustomerSummary> findAllByCompanyId(UUID companyId, Pageable pageable);



}
