package com.fangbuilt.lc_loan_system.features.loan.api;

import com.fangbuilt.lc_loan_system.features.customer.domain.CustomerProfile;
import com.fangbuilt.lc_loan_system.features.customer.service.CustomerService;
import com.fangbuilt.lc_loan_system.features.loan.api.dto.ApplyLoanRequest;
import com.fangbuilt.lc_loan_system.features.loan.api.dto.LoanResponse;
import com.fangbuilt.lc_loan_system.features.loan.domain.Loan;
import com.fangbuilt.lc_loan_system.features.loan.service.LoanService;
import com.fangbuilt.lc_loan_system.features.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Loans", description = "Loan management")
public class LoanController {

  private final LoanService service;
  private final CustomerService customerService;

  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (User) auth.getPrincipal();
  }

  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(summary = "Apply for a loan (Customer only)")
  public ResponseEntity<LoanResponse> applyLoan(@Valid @RequestBody ApplyLoanRequest request) {
    User currentUser = getCurrentUser();
    CustomerProfile customer = customerService.findByUserId(currentUser.getId());

    Loan loan = service.applyLoan(
            customer.getId(),
            request.getAmount(),
            request.getTenorMonths()
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(LoanResponse.from(loan));
  }

  @GetMapping("/my-loans")
  @PreAuthorize("hasRole('CUSTOMER')")
  @Operation(summary = "Get my loans (Customer only)")
  public ResponseEntity<List<LoanResponse>> getMyLoans() {
    List<LoanResponse> loans = service.getMyLoans()
            .stream()
            .map(LoanResponse::from)
            .collect(Collectors.toList());
    return ResponseEntity.ok(loans);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get loan by ID (Customer: own loans only, Admin: all)")
  public ResponseEntity<LoanResponse> getLoanById(@PathVariable UUID id) {
    Loan loan = service.findById(id);
    return ResponseEntity.ok(LoanResponse.from(loan));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get all loans (Admin only)")
  public ResponseEntity<List<LoanResponse>> getAllLoans() {
    List<LoanResponse> loans = service.getAllLoans()
            .stream()
            .map(LoanResponse::from)
            .collect(Collectors.toList());

    return ResponseEntity.ok(loans);
  }
}