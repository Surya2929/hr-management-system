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

            // 2. The ONE HR admin account. Email + password always come from the env variables
            //    HR_ADMIN_EMAIL / HR_ADMIN_PASSWORD (set privately on the server), so they stay fixed.
            if (hrEmail.isBlank() || hrPassword.isBlank()) {
                // Fail safe: with no configured admin, no HR account may log in
                // (otherwise an old account with a known password would stay open)
                for (User old : userRepository.findByRole(User.Role.HR)) {
                    old.setIsActive(false);
                    userRepository.save(old);
                }
                log.error("HR_ADMIN_EMAIL / HR_ADMIN_PASSWORD are not set - all HR logins are disabled until they are set");
                return;
            }
            if (hrPassword.length() < 12) {
                log.warn("HR_ADMIN_PASSWORD is short - use at least 12 characters");
            }

            User hr = userRepository.findByEmail(hrEmail).orElse(null);
            if (hr != null && hr.getRole() != User.Role.HR) {
                log.error("{} belongs to an employee account - choose a different HR_ADMIN_EMAIL", hrEmail);
                return;
            }
            if (hr == null) {
                hr = User.builder().email(hrEmail).role(User.Role.HR).build();
            }
            hr.setPassword(passwordEncoder.encode(hrPassword)); // env value is the source of truth
            hr.setIsActive(true);
            hr.setMustChangePassword(false);
            userRepository.save(hr);
            log.info("HR admin ready: {}", hrEmail);

            // 3. Only that account may be HR - switch off any other HR logins (e.g. old hr@company.com)
            for (User other : userRepository.findByRole(User.Role.HR)) {
                if (!other.getEmail().equalsIgnoreCase(hrEmail) && Boolean.TRUE.equals(other.getIsActive())) {
                    other.setIsActive(false);
                    userRepository.save(other);
                    log.warn("Disabled extra HR account: {}", other.getEmail());
                }
            }
        };
    }
}