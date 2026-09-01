package com.hrapp.controller;

import com.hrapp.dto.LoginRequest;
import com.hrapp.dto.LoginResponse;
import com.hrapp.dto.RegisterRequest;
import com.hrapp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Authenticates HR or EMPLOYEE and returns JWT + role
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/register
     * Public self-registration for EMPLOYEE accounts ONLY
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> registerEmployee(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerEmployee(request));
    }
}
