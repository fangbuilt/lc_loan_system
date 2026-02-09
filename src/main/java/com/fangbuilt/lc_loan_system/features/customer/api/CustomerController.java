package com.fangbuilt.lc_loan_system.features.customer.api;

import com.fangbuilt.lc_loan_system.features.customer.domain.CustomerProfile;
import com.fangbuilt.lc_loan_system.features.customer.service.CustomerService;
import com.fangbuilt.lc_loan_system.features.user.domain.User;
import com.fangbuilt.lc_loan_system.shared.domain.PageRequest;
import com.fangbuilt.lc_loan_system.shared.domain.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Customers", description = "Customer management")
public class CustomerController {

    private final CustomerService service;

    // CUSTOMER endpoints

    @PostMapping("/me/upload-ktp")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Upload KTP (Customer only)")
    public ResponseEntity<Map<String, String>> uploadKtp(
            @RequestParam("file") MultipartFile file
    ) {
        CustomerProfile customer = getCurrentCustomerProfile();
        service.uploadKtp(customer.getId(), file);
        return ResponseEntity.ok(Map.of("message", "KTP uploaded successfully"));
    }

    @PostMapping("/me/upload-salary-slip")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Upload Salary Slip (Customer only)")
    public ResponseEntity<Map<String, String>> uploadSalarySlip(
            @RequestParam("file") MultipartFile file
    ) {
        CustomerProfile customer = getCurrentCustomerProfile();
        service.uploadSalarySlip(customer.getId(), file);
        return ResponseEntity.ok(Map.of("message", "Salary slip uploaded successfully"));
    }

    @GetMapping("/me/ktp")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Download my KTP (Customer only)")
    public ResponseEntity<Resource> downloadMyKtp(HttpServletRequest request) {
        CustomerProfile customer = getCurrentCustomerProfile();
        return downloadDocument(service.getKtpDocument(customer.getId()), request);
    }

    // ADMIN endpoints

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all customers (Admin only)")
    public ResponseEntity<PageResponse<CustomerProfile>> getAllCustomers(PageRequest pageRequest) {
        return ResponseEntity.ok(PageResponse.of(service.findAll(pageRequest)));
    }

    @GetMapping("/{id}/ktp")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Download customer KTP (Admin only)")
    public ResponseEntity<Resource> downloadCustomerKtp(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        return downloadDocument(service.getKtpDocument(id), request);
    }

    @GetMapping("/{id}/salary-slip")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Download customer salary slip (Admin only)")
    public ResponseEntity<Resource> downloadCustomerSalarySlip(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        return downloadDocument(service.getSalarySlipDocument(id), request);
    }

    private CustomerProfile getCurrentCustomerProfile() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return service.findByUserId(user.getId());
    }

    private ResponseEntity<Resource> downloadDocument(Resource resource, HttpServletRequest request) {
        String contentType = null;
        try {
            contentType = request.getServletContext()
                    .getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}