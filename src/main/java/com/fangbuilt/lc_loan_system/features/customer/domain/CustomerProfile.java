package com.fangbuilt.lc_loan_system.features.customer.domain;

import com.fangbuilt.lc_loan_system.features.user.domain.User;
import com.fangbuilt.lc_loan_system.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "customer_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfile extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyIncome;
    
    @Column
    private String ktpPath;
    
    @Column
    private String salarySlipPath;
    
    public boolean hasUploadedKtp() {
        return ktpPath != null && !ktpPath.isEmpty();
    }
    
    public boolean hasUploadedSalarySlip() {
        return salarySlipPath != null && !salarySlipPath.isEmpty();
    }
    
    public boolean hasAllRequiredDocuments() {
        return hasUploadedKtp() && hasUploadedSalarySlip();
    }
}
