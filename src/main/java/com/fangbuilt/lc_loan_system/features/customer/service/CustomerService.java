package com.fangbuilt.lc_loan_system.features.customer.service;

import com.fangbuilt.lc_loan_system.features.customer.domain.CustomerProfile;
import com.fangbuilt.lc_loan_system.features.customer.repository.CustomerProfileRepository;
import com.fangbuilt.lc_loan_system.features.user.domain.User;
import com.fangbuilt.lc_loan_system.shared.exception.BadRequestException;
import com.fangbuilt.lc_loan_system.shared.exception.BusinessRuleViolationException;
import com.fangbuilt.lc_loan_system.shared.service.BaseCrudService;
import com.fangbuilt.lc_loan_system.shared.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService extends BaseCrudService<CustomerProfile, CustomerProfileRepository> {

    private final CustomerProfileRepository repository;
    private final FileStorageService fileStorageService;

    @Override
    protected CustomerProfileRepository getRepository() {
        return repository;
    }

    @Override
    protected String getEntityName() {
        return "CustomerProfile";
    }

    @Override
    protected void updateFields(CustomerProfile existing, CustomerProfile updated) {
        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
        if (updated.getMonthlyIncome() != null) existing.setMonthlyIncome(updated.getMonthlyIncome());
    }

    public CustomerProfile findByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Customer profile not found for user"));
    }

    /**
     * Upload KTP - STRICT RULE: Cannot re-upload if already exists
     */
    @Transactional
    public void uploadKtp(UUID customerId, MultipartFile file) {
        CustomerProfile customer = findById(customerId);

        // STRICT RULE: Cannot upload if already exists
        if (customer.hasUploadedKtp()) {
            throw new BusinessRuleViolationException(
                    "KTP has already been uploaded. Re-upload is not allowed."
            );
        }

        String filename = fileStorageService.storeFile(file);
        customer.setKtpPath(filename);
        repository.save(customer);
        log.info("KTP uploaded for customer: {}", customerId);
    }

    /**
     * Upload Salary Slip - STRICT RULE: Cannot re-upload if already exists
     */
    @Transactional
    public void uploadSalarySlip(UUID customerId, MultipartFile file) {
        CustomerProfile customer = findById(customerId);

        // STRICT RULE: Cannot upload if already exists
        if (customer.hasUploadedSalarySlip()) {
            throw new BusinessRuleViolationException(
                    "Salary slip has already been uploaded. Re-upload is not allowed."
            );
        }

        String filename = fileStorageService.storeFile(file);
        customer.setSalarySlipPath(filename);
        repository.save(customer);
        log.info("Salary slip uploaded for customer: {}", customerId);
    }

    public Resource getKtpDocument(UUID customerId) {
        CustomerProfile customer = findById(customerId);
        if (customer.getKtpPath() == null) {
            throw new BadRequestException("KTP document not found");
        }
        return fileStorageService.loadFileAsResource(customer.getKtpPath());
    }

    public Resource getSalarySlipDocument(UUID customerId) {
        CustomerProfile customer = findById(customerId);
        if (customer.getSalarySlipPath() == null) {
            throw new BadRequestException("Salary slip document not found");
        }
        return fileStorageService.loadFileAsResource(customer.getSalarySlipPath());
    }
}