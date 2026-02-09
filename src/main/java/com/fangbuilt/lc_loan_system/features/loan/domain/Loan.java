package com.fangbuilt.lc_loan_system.features.loan.domain;

import com.fangbuilt.lc_loan_system.features.customer.domain.CustomerProfile;
import com.fangbuilt.lc_loan_system.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loan extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private Integer tenorMonths;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.PENDING;
    
    @Column
    private String rejectionReason;
    
    @Column
    private Integer creditScore;
    
    public boolean isPending() {
        return status == LoanStatus.PENDING;
    }
    
    public boolean isApproved() {
        return status == LoanStatus.APPROVED;
    }
    
    public boolean isRejected() {
        return status == LoanStatus.REJECTED;
    }
    
    public void approve(BigDecimal interestRate, Integer creditScore) {
        this.status = LoanStatus.APPROVED;
        this.interestRate = interestRate;
        this.creditScore = creditScore;
    }
    
    public void reject(String reason, Integer creditScore) {
        this.status = LoanStatus.REJECTED;
        this.rejectionReason = reason;
        this.creditScore = creditScore;
    }
}
