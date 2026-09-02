package com.hrapp.config;

import com.hrapp.entity.Department;
import com.hrapp.entity.User;
import com.hrapp.repository.DepartmentRepository;
import com.hrapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

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

            // 2. Create or reset fixed HR account
            String hrEmail = "hr@company.com";

            User hrUser = userRepository.findByEmail(hrEmail)
                    .orElse(
                            User.builder()
                                    .email(hrEmail)
                                    .role(User.Role.HR)
                                    .isActive(true)
                                    .build()
                    );

            hrUser.setPassword(
                    passwordEncoder.encode("Hr@Secure123")
            );

            hrUser.setRole(User.Role.HR);
            hrUser.setIsActive(true);

            userRepository.save(hrUser);

            System.out.println(">>> HR ACCOUNT READY <<<");
        };
    }
}