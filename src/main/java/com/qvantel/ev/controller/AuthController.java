package com.qvantel.ev.controller;

import com.qvantel.ev.dto.LoginRequest;
import com.qvantel.ev.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qvantel.ev.security.HttpLoggingFilter;
import com.qvantel.ev.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log =
            LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response =
                authService.login(
                        request.getUsername(),
                        request.getPassword()
                );
        log.info("User: " + request.getUsername() + ", Password: " + request.getPassword());
        return ResponseEntity.ok(response);
    }
}

