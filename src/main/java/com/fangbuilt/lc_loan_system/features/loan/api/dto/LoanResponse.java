package com.fangbuilt.lc_loan_system.features.loan.api.dto;

import com.fangbuilt.lc_loan_system.features.loan.domain.Loan;
import com.fangbuilt.lc_loan_system.features.loan.domain.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

  private UUID id;
  private BigDecimal amount;
  private Integer tenorMonths;
  private BigDecimal interestRate;
  private LoanStatus status;
  private Integer creditScore;
  private String rejectionReason;

  // Customer info (simplified)
  private UUID customerId;
  private String customerName;
  private String customerEmail;

  // Timestamps
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  /**
   * Factory method: convert Entity -> DTO
   */
  public static LoanResponse from(Loan loan) {
    return LoanResponse.builder()
            .id(loan.getId())
            .amount(loan.getAmount())
            .tenorMonths(loan.getTenorMonths())
            .interestRate(loan.getInterestRate())
            .status(loan.getStatus())
            .creditScore(loan.getCreditScore())
            .rejectionReason(loan.getRejectionReason())
            .customerId(loan.getCustomer().getId())
            .customerName(loan.getCustomer().getName())
            .customerEmail(loan.getCustomer().getEmail())
            .createdAt(loan.getCreatedAt())
            .updatedAt(loan.getUpdatedAt())
            .build();
  }
}