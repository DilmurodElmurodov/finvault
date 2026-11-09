package com.finvault.service.impl;

import com.finvault.dto.request.LoginRequest;
import com.finvault.dto.request.RegisterRequest;
import com.finvault.dto.response.AuthResponse;
import com.finvault.dto.response.UserResponse;
import com.finvault.entity.User;
import com.finvault.exception.BusinessException;
import com.finvault.exception.DuplicateResourceException;
import com.finvault.exception.ResourceNotFoundException;
import com.finvault.repository.UserRepository;
import com.finvault.security.jwt.JwtUtil;
import com.finvault.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Auth Service Implementation - Registration, Login, Token management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .username(request.getUsername().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .emailVerificationToken(UUID.randomUUID().toString())
                .role(User.Role.USER)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {} ({})", user.getUsername(), user.getEmail());

        // TODO: Send verification email via EmailService

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        Map<String, Object> claims = Map.of("role", user.getRole().name(), "userId", user.getId().toString());

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(claims, userDetails))
                .refreshToken(jwtUtil.generateRefreshToken(userDetails))
                .expiresIn(86400)
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        Map<String, Object> claims = Map.of("role", user.getRole().name(), "userId", user.getId().toString());

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(claims, userDetails))
                .refreshToken(jwtUtil.generateRefreshToken(userDetails))
                .expiresIn(86400)
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
            throw new BusinessException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username));

        Map<String, Object> claims = Map.of("role", user.getRole().name(), "userId", user.getId().toString());

        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(claims, userDetails))
                .refreshToken(jwtUtil.generateRefreshToken(userDetails))
                .expiresIn(86400)
                .user(mapToUserResponse(user))
                .build();
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        user.setPasswordResetToken(UUID.randomUUID().toString());
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        // TODO: Send password reset email
        log.info("Password reset requested for: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        userRepository.save(user);
        log.info("Password reset completed for user: {}", user.getEmail());
    }

    @Override
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BusinessException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getEmail());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .profilePicture(user.getProfilePicture())
                .emailVerified(user.getEmailVerified())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
