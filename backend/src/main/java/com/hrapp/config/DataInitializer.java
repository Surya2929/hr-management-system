package com.hrapp.config;

import com.hrapp.entity.Department;
import com.hrapp.entity.User;
import com.hrapp.repository.DepartmentRepository;
import com.hrapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    // Read from env vars HR_ADMIN_EMAIL / HR_ADMIN_PASSWORD (see application.properties)
    @Value("${app.hr-admin.email}")
    private String hrEmail;

    @Value("${app.hr-admin.password}")
    private String hrPassword;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {

            // 1. Seed departments if none exist
            if (departmentRepository.count() == 0) {

                departmentRepository.save(
                        Department.builder()
                                .name("Engineering")
                                .description("Software Development and IT Operations")
                                .build()
                );

                departmentRepository.save(
                        Department.builder()
                                .name("Human Resources")
                                .description("People Ops & Talent Acquisition")
                                .build()
                );

                departmentRepository.save(
                        Department.builder()
                                .name("Marketing")
                                .description("Marketing and Communications")
                                .build()
                );

                departmentRepository.save(
                        Department.builder()
                                .name("Finance")
                                .description("Accounting and Financial Planning")
                                .build()
                );
            }

            // 2. Create the HR admin account from env variables (only the first time)
            if (hrEmail.isBlank() || hrPassword.isBlank()) {
                log.warn("HR_ADMIN_EMAIL / HR_ADMIN_PASSWORD not set - skipping HR admin creation");
                return;
            }
            if (userRepository.existsByEmail(hrEmail)) {
                log.info("HR admin {} already exists - password left unchanged", hrEmail);
                return;
            }

            userRepository.save(User.builder()
                    .email(hrEmail)
                    .password(passwordEncoder.encode(hrPassword))
                    .role(User.Role.HR)
                    .isActive(true)
                    .build());
            log.info("HR admin account created: {}", hrEmail);
        };
    }
}