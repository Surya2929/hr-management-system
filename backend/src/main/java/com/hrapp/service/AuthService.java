package com.hrapp.service;

import com.hrapp.config.JwtUtil;
import com.hrapp.dto.LoginRequest;
import com.hrapp.dto.LoginResponse;
import com.hrapp.entity.User;
import com.hrapp.exception.AccountNotActivatedException;
import com.hrapp.repository.UserRepository;
import com.hrapp.entity.PasswordResetToken;
import com.hrapp.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository resetTokenRepository;

    // Used to build the reset link printed in the log
    @Value("${app.frontend.url}")
    private String frontendUrl;

    // Login for both HR and EMPLOYEE.
    // Wrong email and wrong password give the SAME error, so attackers can't find valid emails.
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // A temporary password from HR cannot be used to log in - activate the account first
        if (Boolean.TRUE.equals(user.getMustChangePassword())) {
            throw new AccountNotActivatedException(
                    "Your account is not activated yet. Use 'Activate Account' to set your own password.");
        }

        return buildLoginResponse(user);
    }

    // Activation step 1: check the email + temporary password that HR gave the employee
    public void verifyActivation(String email, String temporaryPassword) {
        findUserForActivation(email, temporaryPassword);
    }

    // Activation step 2: check again, save the employee's own password, log them in
    @Transactional
    public LoginResponse completeActivation(String email, String temporaryPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }
        User user = findUserForActivation(email, temporaryPassword);
        if (newPassword.equals(temporaryPassword)) {
            throw new IllegalArgumentException("New password must be different from the temporary password");
        }

        user.setPassword(passwordEncoder.encode(newPassword)); // BCrypt hash
        user.setMustChangePassword(false);                     // account is now active
        userRepository.save(user);
        return buildLoginResponse(user);
    }

    // Shared check for both activation steps
    private User findUserForActivation(String email, String temporaryPassword) {
        User user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(temporaryPassword, u.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or temporary password"));

        if (!Boolean.TRUE.equals(user.getMustChangePassword())) {
            throw new IllegalArgumentException("This account is already active. Please sign in.");
        }
        return user;
    }

    private LoginResponse buildLoginResponse(User user) {
        return LoginResponse.builder()
                .token(jwtUtil.generateToken(user))
                .role(user.getRole().name())
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }

    // Logged-in user changes their own password (old password must be correct)
    @Transactional
    public void changePassword(User currentUser, String oldPassword, String newPassword) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new BadCredentialsException("Invalid user"));

        // HR password is fixed by the server's HR_ADMIN_PASSWORD - change it there instead
        if (user.getRole() == User.Role.HR) {
            throw new IllegalArgumentException("The HR password is managed on the server and cannot be changed here");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword)); // BCrypt hash
        userRepository.save(user);
    }

    // Step 1 of forgot password: create a 30-minute token and print the link in the log.
    // Caller always gets the same response, so nobody can check which emails exist.
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email)
                .filter(user -> user.getRole() != User.Role.HR) // HR password is managed on the server
                .ifPresent(user -> {
            resetTokenRepository.deleteByUser(user); // only one active token per user

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .build();
            resetTokenRepository.save(resetToken);

            // No email service yet - the link is printed in the server log
            log.info("Password reset link for {}: {}/login?resetToken={}",
                    email, frontendUrl, resetToken.getToken());
        });
    }

    // Step 2 of forgot password: check the token, set the new password, delete the token
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));

        if (resetToken.isExpired()) {
            resetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Invalid or expired reset link");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
        resetTokenRepository.delete(resetToken); // token can be used only once
    }
}
