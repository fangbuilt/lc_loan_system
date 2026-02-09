package com.fangbuilt.lc_loan_system.features.customer.api.dto;

import com.fangbuilt.lc_loan_system.features.customer.domain.CustomerProfile;
import com.fangbuilt.lc_loan_system.security.domain.Role;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCustomerRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    private String phone;
    
    private Role role = Role.CUSTOMER;
    
    @DecimalMin(value = "0", message = "Monthly income must be positive")
    private BigDecimal monthlyIncome;
    
    public CustomerProfile toEntity() {
        CustomerProfile customer = new CustomerProfile();
        customer.setEmail(email);
        customer.setMonthlyIncome(monthlyIncome);
        return customer;
    }
}
