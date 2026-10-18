package com.hrapp.controller;

import com.hrapp.dto.ChangePasswordRequest;
import com.hrapp.dto.ForgotPasswordRequest;
import com.hrapp.dto.LoginRequest;
import com.hrapp.dto.LoginResponse;
import com.hrapp.dto.RegisterRequest;
import com.hrapp.dto.ResetPasswordRequest;
import com.hrapp.entity.User;
import com.hrapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "Login, registration and passwords")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login (HR or Employee) and get a JWT")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Public self-registration - always creates an EMPLOYEE account")
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> registerEmployee(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerEmployee(request));
    }

    @Operation(summary = "Change my password (needs login)")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUser, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @Operation(summary = "Request a password reset link (printed in the server log)")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        // Same answer whether the email exists or not
        return ResponseEntity.ok(Map.of("message",
                "If this email is registered, a password reset link has been sent."));
    }

    @Operation(summary = "Set a new password using the reset token")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password has been reset. You can now sign in."));
    }
}
