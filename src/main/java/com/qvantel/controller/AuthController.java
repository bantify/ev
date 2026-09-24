package com.qvantel.controller;

import com.qvantel.dto.LoginRequest;
import com.qvantel.dto.LoginResponse;
import com.qvantel.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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

        return ResponseEntity.ok(response);
    }
}

