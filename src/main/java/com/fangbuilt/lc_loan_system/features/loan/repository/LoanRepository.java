package com.fangbuilt.lc_loan_system.features.loan.repository;

import com.fangbuilt.lc_loan_system.features.loan.domain.Loan;
import com.fangbuilt.lc_loan_system.features.loan.domain.LoanStatus;
import com.fangbuilt.lc_loan_system.shared.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends BaseRepository<Loan> {

    List<Loan> findByCustomerId(UUID customerId);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Loan l " +
            "WHERE l.customer.id = :customerId AND l.status = :status AND l.deletedAt IS NULL")
    boolean existsByCustomerIdAndStatus(@Param("customerId") UUID customerId,
                                        @Param("status") LoanStatus status);
}