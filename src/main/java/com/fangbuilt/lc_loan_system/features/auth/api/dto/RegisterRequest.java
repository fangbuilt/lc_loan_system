package com.fangbuilt.lc_loan_system.features.auth.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class RegisterRequest {
    @NotBlank private String username;
    @NotBlank @Size(min = 6) private String password;
    @NotBlank private String name;
    @NotBlank @Email private String email;
    @NotNull
    @DecimalMin("0") private BigDecimal monthlyIncome;
}
