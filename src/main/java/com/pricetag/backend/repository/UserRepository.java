package com.pricetag.backend.repository;

import com.pricetag.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.company WHERE u.email = :email")
    Optional<User> findByEmailWithCompany(String email);

    boolean existsByEmail(String email);
}
