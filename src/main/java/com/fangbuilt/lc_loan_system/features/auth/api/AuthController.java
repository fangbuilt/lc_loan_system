package com.fangbuilt.lc_loan_system.features.auth.api;

import com.fangbuilt.lc_loan_system.features.auth.api.dto.LoginRequest;
import com.fangbuilt.lc_loan_system.features.auth.api.dto.LoginResponse;
import com.fangbuilt.lc_loan_system.features.auth.api.dto.RefreshTokenRequest;
import com.fangbuilt.lc_loan_system.features.auth.api.dto.RegisterRequest;
import com.fangbuilt.lc_loan_system.features.auth.service.AuthService;
import com.fangbuilt.lc_loan_system.features.customer.domain.CustomerProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new customer")
    public ResponseEntity<CustomerProfile> register(@Valid @RequestBody RegisterRequest request) {
        CustomerProfile customer = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login and get access + refresh tokens")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout (revoke refresh token)")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
