package com.hrapp.config;

import com.hrapp.entity.Department;
import com.hrapp.entity.User;
import com.hrapp.repository.DepartmentRepository;
import com.hrapp.repository.EmployeeRepository;
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
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // 1. Seed initial departments if none exist
            if (departmentRepository.count() == 0) {
                departmentRepository.save(Department.builder()
                        .name("Engineering")
                        .description("Software Development and IT Operations")
                        .build());
                departmentRepository.save(Department.builder()
                        .name("Human Resources")
                        .description("People Ops & Talent Acquisition")
                        .build());
                departmentRepository.save(Department.builder()
                        .name("Marketing")
                        .description("Marketing and Communications")
                        .build());
                departmentRepository.save(Department.builder()
                        .name("Finance")
                        .description("Accounting and Financial Planning")
                        .build());
            }

            // 2. Seed the fixed HR account if it doesn't exist
            // NOTE: HR is a login/admin account only — it intentionally does NOT
            // get an Employee record, since HR is not listed in Employee Management.
            String hrEmail = "hr@company.com";
            if (!userRepository.existsByEmail(hrEmail)) {
                User hrUser = User.builder()
                        .email(hrEmail)
                        .password(passwordEncoder.encode("Hr@Secure123"))
                        .role(User.Role.HR)
                        .isActive(true)
                        .build();
                userRepository.save(hrUser);
                System.out.println(">>> SEEDED FIXED HR ACCOUNT: hr@company.com / Hr@Secure123 <<<");
            }
        };
    }
}