package com.fangbuilt.lc_loan_system.features.user.repository;

import com.fangbuilt.lc_loan_system.features.user.domain.User;
import com.fangbuilt.lc_loan_system.shared.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}