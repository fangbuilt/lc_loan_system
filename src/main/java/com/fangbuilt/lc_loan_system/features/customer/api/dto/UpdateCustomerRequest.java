package com.fangbuilt.lc_loan_system.features.customer.api.dto;

import com.fangbuilt.lc_loan_system.features.customer.domain.CustomerProfile;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCustomerRequest {

    private String fullName;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @DecimalMin(value = "0", message = "Monthly income must be positive")
    private BigDecimal monthlyIncome;

    public void updateEntity(CustomerProfile customer) {
        if (fullName != null) customer.setName(fullName);
        if (monthlyIncome != null) customer.setMonthlyIncome(monthlyIncome);
    }
}