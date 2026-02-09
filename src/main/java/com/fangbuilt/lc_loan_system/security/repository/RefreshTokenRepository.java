package com.fangbuilt.lc_loan_system.security.repository;

import com.fangbuilt.lc_loan_system.security.domain.RefreshToken;
import com.fangbuilt.lc_loan_system.shared.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends BaseRepository<RefreshToken> {
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token " +
           "AND rt.revokedAt IS NULL AND rt.deletedAt IS NULL")
    Optional<RefreshToken> findByTokenAndNotRevoked(@Param("token") String token);
    
    void deleteByCustomerId(UUID customerId);
}
