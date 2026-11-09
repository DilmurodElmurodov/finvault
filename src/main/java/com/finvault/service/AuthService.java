package com.finvault.service;

import com.finvault.dto.request.LoginRequest;
import com.finvault.dto.request.RegisterRequest;
import com.finvault.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
    void verifyEmail(String token);
}
