package com.fangbuilt.lc_loan_system.features.loan.service;

import com.fangbuilt.lc_loan_system.features.customer.domain.CustomerProfile;
import com.fangbuilt.lc_loan_system.features.customer.service.CustomerService;
import com.fangbuilt.lc_loan_system.features.loan.domain.Loan;
import com.fangbuilt.lc_loan_system.features.loan.domain.LoanStatus;
import com.fangbuilt.lc_loan_system.features.loan.repository.LoanRepository;
import com.fangbuilt.lc_loan_system.features.user.domain.User;
import com.fangbuilt.lc_loan_system.shared.exception.BadRequestException;
import com.fangbuilt.lc_loan_system.shared.exception.BusinessRuleViolationException;
import com.fangbuilt.lc_loan_system.shared.service.BaseCrudService;
import com.fangbuilt.lc_loan_system.shared.service.CreditScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService extends BaseCrudService<Loan, LoanRepository> {
    
    private final LoanRepository repository;
    private final CustomerService customerService;
    private final CreditScoreService creditScoreService;
    
    @Override
    protected LoanRepository getRepository() {
        return repository;
    }
    
    @Override
    protected String getEntityName() {
        return "Loan";
    }
    
    @Override
    protected void updateFields(Loan existing, Loan updated) {
        // Loan tidak bisa diupdate setelah dibuat (hanya status yang berubah via approval)
        throw new BadRequestException("Loan cannot be updated directly");
    }
    
    /**
     * Apply for a new loan with all business rule validations
     */
    @Transactional
    public Loan applyLoan(UUID customerId, BigDecimal amount, Integer tenorMonths) {
        log.info("Processing loan application for customer: {}, amount: {}, tenor: {}", 
            customerId, amount, tenorMonths);
        
        CustomerProfile customer = customerService.findById(customerId);
        
        // BUSINESS RULE 1: Document validation
        validateDocuments(customer);
        
        // BUSINESS RULE 2: No multiple pending loans
        validateNoPendingLoans(customerId);
        
        // BUSINESS RULE 3: Debt to Income Ratio
        validateDebtToIncomeRatio(customer, amount);
        
        // Create loan entity
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setAmount(amount);
        loan.setTenorMonths(tenorMonths);
        loan.setStatus(LoanStatus.PENDING);
        loan.setInterestRate(BigDecimal.ZERO); // Will be set after credit score check
        
        // Save first to get ID
        loan = repository.save(loan);
        
        // BUSINESS RULE 4: Credit Score Integration
        processLoanApproval(loan);
        
        return repository.save(loan);
    }
    
    /**
     * BUSINESS RULE 1: Customer must upload KTP and Salary Slip
     */
    private void validateDocuments(CustomerProfile customer) {
        if (!customer.hasUploadedKtp()) {
            throw new BusinessRuleViolationException(
                "KTP document must be uploaded before applying for a loan"
            );
        }
        
        if (!customer.hasUploadedSalarySlip()) {
            throw new BusinessRuleViolationException(
                "Salary slip document must be uploaded before applying for a loan"
            );
        }
        
        log.debug("Document validation passed for customer: {}", customer.getId());
    }
    
    /**
     * BUSINESS RULE 2: Customer cannot have multiple pending loans
     */
    private void validateNoPendingLoans(UUID customerId) {
        boolean hasPendingLoan = repository.existsByCustomerIdAndStatus(customerId, LoanStatus.PENDING);
        
        if (hasPendingLoan) {
            throw new BusinessRuleViolationException(
                "You already have a pending loan application. Please wait for approval or rejection."
            );
        }
        
        log.debug("No pending loan check passed for customer: {}", customerId);
    }
    
    /**
     * BUSINESS RULE 3: Debt to Income Ratio
     * maxLoanAllowed = monthlyIncome × 12
     */
    private void validateDebtToIncomeRatio(CustomerProfile customer, BigDecimal amount) {
        BigDecimal maxLoanAllowed = customer.getMonthlyIncome().multiply(BigDecimal.valueOf(12));
        
        if (amount.compareTo(maxLoanAllowed) > 0) {
            String message = String.format(
                "Loan amount (%s) exceeds maximum allowed (%s) based on monthly income (%s)",
                amount, maxLoanAllowed, customer.getMonthlyIncome()
            );
            throw new BusinessRuleViolationException(message);
        }
        
        log.debug("Debt to income ratio check passed. Amount: {}, Max allowed: {}", 
            amount, maxLoanAllowed);
    }
    
    /**
     * BUSINESS RULE 4: Credit Score Integration
     * 
     * Score >= 700: APPROVED (10% interest)
     * Score 600-699: APPROVED (15% interest)
     * Score < 600: REJECTED
     */
    private void processLoanApproval(Loan loan) {
        try {
            int creditScore = creditScoreService.getCreditScore(loan.getCustomer().getId().toString());
            
            log.info("Credit score received for loan {}: {}", loan.getId(), creditScore);
            
            if (creditScore >= 700) {
                loan.approve(BigDecimal.valueOf(10.0), creditScore);
                log.info("Loan {} APPROVED with 10% interest (excellent credit score: {})", 
                    loan.getId(), creditScore);
                
            } else if (creditScore >= 600) {
                loan.approve(BigDecimal.valueOf(15.0), creditScore);
                log.info("Loan {} APPROVED with 15% interest (good credit score: {})", 
                    loan.getId(), creditScore);
                
            } else {
                loan.reject("Credit score too low", creditScore);
                log.info("Loan {} REJECTED (low credit score: {})", loan.getId(), creditScore);
            }
            
        } catch (Exception e) {
            log.error("Credit score service failed for loan {}: {}", loan.getId(), e.getMessage());
            throw new BadRequestException(
                "Failed to process loan application due to credit score service error. Please try again later."
            );
        }
    }
    
    /**
     * Get all loans for current logged-in customer
     */
    public List<Loan> getMyLoans() {
        User currentUser = getCurrentUser();
        CustomerProfile customer = customerService.findByUserId(currentUser.getId());
        return repository.findByCustomerId(customer.getId());
    }
    
    /**
     * Get all loans (Admin only)
     */
    @Cacheable(value = "loan")
    public List<Loan> getAllLoans() {
        return repository.findAll();
    }
    
    /**
     * Get loan by ID with authorization check
     */
    @Override
    public Loan findById(UUID id) {
        Loan loan = super.findById(id);
        
        User currentUser = getCurrentUser();
        
        // If not admin, can only view own loans
        if (!currentUser.getRole().name().equals("ADMIN")) {
            CustomerProfile customer = customerService.findByUserId(currentUser.getId());
            if (!loan.getCustomer().getId().equals(customer.getId())) {
                throw new BusinessRuleViolationException("You can only view your own loans");
            }
        }
        
        return loan;
    }
    
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }
}
