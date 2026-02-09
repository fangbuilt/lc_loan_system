package com.fangbuilt.lc_loan_system.features.loan.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ApplyLoanRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Tenor is required")
    @Min(value = 1, message = "Tenor must be at least 1 month")
    private Integer tenorMonths;
}