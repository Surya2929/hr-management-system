package com.hrapp.service;

import com.hrapp.config.JwtUtil;
import com.hrapp.dto.LoginRequest;
import com.hrapp.dto.LoginResponse;
import com.hrapp.dto.RegisterRequest;
import com.hrapp.entity.Department;
import com.hrapp.entity.Employee;
import com.hrapp.entity.User;
import com.hrapp.repository.DepartmentRepository;
import com.hrapp.repository.EmployeeRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
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

        String token = jwtUtil.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }

    // Public Employee Self-Registration (STRICTLY creates EMPLOYEE role only)
    @Transactional
    public LoginResponse registerEmployee(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        // 1. Create user with strictly EMPLOYEE role
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.EMPLOYEE) // never allow HR creation here
                .isActive(true)
                .build();
        user = userRepository.save(user);

        // 2. Department assignment
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElse(null);
        }

        // 3. Create employee profile
        Employee employee = Employee.builder()
                .user(user)
                .department(department)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .designation(request.getDesignation() != null ? request.getDesignation() : "Employee")
                .dateJoined(LocalDate.now())
                .build();
        employeeRepository.save(employee);

        // 4. Generate JWT so user is immediately logged in
        String token = jwtUtil.generateToken(user);

        return LoginResponse.builder()
                .token(token)
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
        userRepository.findByEmail(email).ifPresent(user -> {
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
        userRepository.save(user);
        resetTokenRepository.delete(resetToken); // token can be used only once
    }
}
