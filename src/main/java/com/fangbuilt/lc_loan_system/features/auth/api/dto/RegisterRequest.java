package com.fangbuilt.lc_loan_system.features.auth.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class RegisterRequest {
  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 30, message = "Username must be between 6 and 30 characters")
  @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Username can only contain alphanumeric characters and underscores")
  private String username;

  @NotBlank
  @Size(min = 6)
  private String password;

  @NotBlank
  private String fullName;

  @NotBlank
  @Email
  private String email;

  private String phone;

  @NotNull
  @DecimalMin("0")
  private BigDecimal monthlyIncome;
}
