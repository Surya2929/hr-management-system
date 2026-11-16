package com.hrapp.controller;

import com.hrapp.config.RateLimiter;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Tag(name = "Auth", description = "Login, account activation and passwords")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Attempt limits (per 15 minutes) that stop password guessing
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int PER_ACCOUNT = 10; // tries on one email
    private static final int PER_IP      = 50; // tries from one IP across all emails

    private final AuthService authService;
    private final RateLimiter rateLimiter;

    // Counts one attempt for this email and this IP; throws 429 when over the limit
    private void limit(String action, String email, HttpServletRequest http) {
        if (email != null) {
            rateLimiter.check(action + ":email:" + email.trim().toLowerCase(), PER_ACCOUNT, WINDOW);
        }
        rateLimiter.check(action + ":ip:" + RateLimiter.clientIp(http), PER_IP, WINDOW);
    }

    @Operation(summary = "Login (HR or Employee) and get a JWT")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        limit("login", request.getEmail(), http);
        LoginResponse response = authService.login(request);
        rateLimiter.reset("login:email:" + request.getEmail().trim().toLowerCase()); // success: forget earlier typos
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activation step 1: verify email + temporary password from HR")
    @PostMapping("/activate/verify")
    public ResponseEntity<Map<String, String>> verifyActivation(@Valid @RequestBody ActivateAccountRequest request,
                                                                HttpServletRequest http) {
        limit("activate", request.getEmail(), http);
        authService.verifyActivation(request.getEmail(), request.getTemporaryPassword());
        return ResponseEntity.ok(Map.of("message", "Verified. Now set your own password."));
    }

    @Operation(summary = "Activation step 2: set your own password and log in")
    @PostMapping("/activate/complete")
    public ResponseEntity<LoginResponse> completeActivation(@Valid @RequestBody ActivateAccountRequest request,
                                                            HttpServletRequest http) {
        limit("activate", request.getEmail(), http);
        LoginResponse response = authService.completeActivation(
                request.getEmail(), request.getTemporaryPassword(), request.getNewPassword());
        rateLimiter.reset("activate:email:" + request.getEmail().trim().toLowerCase());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Change my password (needs login). Returns a new token; other devices are logged out.")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        String newToken = authService.changePassword(currentUser, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully", "token", newToken));
    }

    @Operation(summary = "Request a password reset link (printed in the server log)")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                              HttpServletRequest http) {
        limit("forgot", request.getEmail(), http);
        authService.forgotPassword(request.getEmail());
        // Same answer whether the email exists or not
        return ResponseEntity.ok(Map.of("message",
                "If this email is registered, a password reset link has been sent."));
    }

    @Operation(summary = "Set a new password using the reset token")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                                             HttpServletRequest http) {
        limit("reset", null, http);
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password has been reset. You can now sign in."));
    }
}
