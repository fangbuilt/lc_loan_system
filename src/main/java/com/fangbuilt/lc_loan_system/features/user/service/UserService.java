package com.fangbuilt.lc_loan_system.features.user.service;

import com.fangbuilt.lc_loan_system.features.user.domain.User;
import com.fangbuilt.lc_loan_system.features.user.repository.UserRepository;
import com.fangbuilt.lc_loan_system.security.domain.Role;
import com.fangbuilt.lc_loan_system.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public User createUser(String username, String password, Role role) {
        if (repository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return repository.save(user);
    }
}