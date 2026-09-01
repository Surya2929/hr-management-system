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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // Login for both HR and EMPLOYEE
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getEmail()));

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
}
