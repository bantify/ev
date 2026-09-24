
package com.qvantel.ev.service;

import com.qvantel.ev.dto.LoginResponse;
import com.qvantel.ev.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(
            String username,
            String password) {

        // Temporary user for testing
        String storedUsername = "admin";

        String storedPassword =
                passwordEncoder.encode("password123");

        if (!storedUsername.equals(username)
                || !passwordEncoder.matches(
                        password,
                        storedPassword)) {

            throw new RuntimeException(
                    "Invalid username or password"
            );
        }

        String accessToken =
                jwtService.generateAccessToken(username);

        String refreshToken =
                jwtService.generateRefreshToken(username);

        return new LoginResponse(
                accessToken,
                refreshToken,
                900
        );
    }
}

