package com.fangbuilt.lc_loan_system.features.customer.repository;

import com.fangbuilt.lc_loan_system.features.customer.domain.CustomerProfile;
import com.fangbuilt.lc_loan_system.shared.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerProfileRepository extends BaseRepository<CustomerProfile> {

    @Query("SELECT cp FROM CustomerProfile cp WHERE cp.user.id = :userId AND cp.deletedAt IS NULL")
    Optional<CustomerProfile> findByUserId(@Param("userId") UUID userId);

    boolean existsByEmail(String email);

    Optional<CustomerProfile> findByEmail(String email);
}