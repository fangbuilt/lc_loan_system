package com.fangbuilt.lc_loan_system.features.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 6, max = 30, message = "Username must be between 6 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Username can only contain alphanumeric characters and underscores")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
}
