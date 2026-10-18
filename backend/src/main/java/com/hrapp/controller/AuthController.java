package com.hrapp.controller;

import com.hrapp.dto.ActivateAccountRequest;
import com.hrapp.dto.ChangePasswordRequest;
import com.hrapp.dto.ForgotPasswordRequest;
import com.hrapp.dto.LoginRequest;
import com.hrapp.dto.LoginResponse;
import com.hrapp.dto.ResetPasswordRequest;
import com.hrapp.entity.User;
import com.hrapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "Login, account activation and passwords")
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

    @Operation(summary = "Activation step 1: verify email + temporary password from HR")
    @PostMapping("/activate/verify")
    public ResponseEntity<Map<String, String>> verifyActivation(@Valid @RequestBody ActivateAccountRequest request) {
        authService.verifyActivation(request.getEmail(), request.getTemporaryPassword());
        return ResponseEntity.ok(Map.of("message", "Verified. Now set your own password."));
    }

    @Operation(summary = "Activation step 2: set your own password and log in")
    @PostMapping("/activate/complete")
    public ResponseEntity<LoginResponse> completeActivation(@Valid @RequestBody ActivateAccountRequest request) {
        return ResponseEntity.ok(authService.completeActivation(
                request.getEmail(), request.getTemporaryPassword(), request.getNewPassword()));
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
