package com.fangbuilt.lc_loan_system.features.auth.service;

import com.fangbuilt.lc_loan_system.features.auth.api.dto.RegisterRequest;
import com.fangbuilt.lc_loan_system.features.customer.domain.CustomerProfile;
import com.fangbuilt.lc_loan_system.features.customer.repository.CustomerProfileRepository;
import com.fangbuilt.lc_loan_system.features.user.domain.User;
import com.fangbuilt.lc_loan_system.features.auth.api.dto.LoginResponse;
import com.fangbuilt.lc_loan_system.features.user.service.UserService;
import com.fangbuilt.lc_loan_system.security.domain.RefreshToken;
import com.fangbuilt.lc_loan_system.security.domain.Role;
import com.fangbuilt.lc_loan_system.security.jwt.JwtProperties;
import com.fangbuilt.lc_loan_system.security.jwt.JwtService;
import com.fangbuilt.lc_loan_system.security.repository.RefreshTokenRepository;
import com.fangbuilt.lc_loan_system.shared.exception.BadRequestException;
import com.fangbuilt.lc_loan_system.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final CustomerProfileRepository customerProfileRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public CustomerProfile register(RegisterRequest request) {
        if (customerProfileRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                Role.CUSTOMER
        );

        CustomerProfile profile = new CustomerProfile();
        profile.setUser(user);
        profile.setName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setMonthlyIncome(request.getMonthlyIncome());

        return customerProfileRepository.save(profile);
    }
    
    @Transactional
    public LoginResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );
        
        CustomerProfile customer = (CustomerProfile) authentication.getPrincipal();
        
        String accessToken = jwtService.generateAccessToken((UserDetails) customer);
        String refreshTokenString = jwtService.generateRefreshToken((UserDetails) customer);
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenString);
        refreshToken.setCustomer(customer);
        refreshToken.setExpiresAt(
            LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000)
        );
        refreshTokenRepository.save(refreshToken);
        
        return new LoginResponse(
            accessToken,
            refreshTokenString,
            jwtProperties.getAccessTokenExpiration() / 1000
        );
    }
    
    @Transactional
    public LoginResponse refreshAccessToken(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndNotRevoked(refreshTokenString)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        
        if (refreshToken.isExpired()) {
            throw new UnauthorizedException("Refresh token has expired");
        }
        
        CustomerProfile customer = refreshToken.getCustomer();
        String newAccessToken = jwtService.generateAccessToken((UserDetails) customer);
        
        return new LoginResponse(
            newAccessToken,
            refreshTokenString,
            jwtProperties.getAccessTokenExpiration() / 1000
        );
    }
    
    @Transactional
    public void logout(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndNotRevoked(refreshTokenString)
            .orElseThrow(() -> new BadRequestException("Refresh token not found"));
        
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }
}
