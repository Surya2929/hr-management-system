package com.hrapp.service;

import com.hrapp.util.InputCheck;
import com.hrapp.exception.BadRequestException;
import com.hrapp.exception.ResourceNotFoundException;
import com.hrapp.entity.Department;
import com.hrapp.entity.Employee;
import com.hrapp.entity.User;
import com.hrapp.dto.CreateEmployeeRequest;
import com.hrapp.dto.CreateEmployeeResponse;
import com.hrapp.util.TempPasswordGenerator;
import com.hrapp.repository.AttendanceRepository;
import com.hrapp.repository.DepartmentRepository;
import com.hrapp.repository.LeaveRequestRepository;
import com.hrapp.repository.EmployeeRepository;
import com.hrapp.repository.PasswordResetTokenRepository;
import com.hrapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // How long a temporary password from HR works (application.properties, default 7 days)
    @Value("${app.temp-password.valid-days:7}")
    private int tempPasswordValidDays;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    // Search by first name / last name / designation + optional department, one page at a time
    public Page<Employee> searchEmployees(String search, Long departmentId, Pageable pageable) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return employeeRepository.search(term, departmentId, pageable);
    }

    // HR adds a new hire: creates the login (with a temporary password) and the employee profile
    @Transactional
    public CreateEmployeeResponse createEmployee(CreateEmployeeRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        String temporaryPassword = TempPasswordGenerator.generate();

        User user = userRepository.save(User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(temporaryPassword)) // only the hash is stored
                .role(User.Role.EMPLOYEE)
                .isActive(true)
                .mustChangePassword(true) // employee must activate before the first login
                .tempPasswordExpiresAt(LocalDateTime.now().plusDays(tempPasswordValidDays))
                .build());

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
        }

        Employee employee = employeeRepository.save(Employee.builder()
                .user(user)
                .department(department)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .designation(request.getDesignation() == null || request.getDesignation().isBlank()
                        ? "Employee" : request.getDesignation())
                .dateJoined(LocalDate.now())
                .build());

        // Returned once so HR can hand it to the employee
        return new CreateEmployeeResponse(employee, temporaryPassword);
    }

    // HR issues a fresh temporary password (employee lost it, or forgot their password)
    @Transactional
    public String resetTemporaryPassword(Long employeeId) {
        User user = getEmployeeById(employeeId).getUser();
        String temporaryPassword = TempPasswordGenerator.generate();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true); // they must activate again with the new temp password
        user.setTempPasswordExpiresAt(LocalDateTime.now().plusDays(tempPasswordValidDays));
        user.setPasswordChangedAt(LocalDateTime.now()); // logs the employee out everywhere
        userRepository.save(user);
        return temporaryPassword;
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    public Employee getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found for user: " + userId));
    }

    @Transactional
    public Employee updateEmployee(Long id, Map<String, Object> updates) {
        Employee employee = getEmployeeById(id);

        // Each field is checked before saving: names cannot be blank, lengths match the DB columns
        if (updates.containsKey("firstName"))
            employee.setFirstName(InputCheck.requiredText(updates.get("firstName"), "First name", 100));

        if (updates.containsKey("lastName"))
            employee.setLastName(InputCheck.requiredText(updates.get("lastName"), "Last name", 100));

        if (updates.containsKey("phone"))
            employee.setPhone(InputCheck.optionalText(updates.get("phone"), "Phone", 20));

        if (updates.containsKey("designation"))
            employee.setDesignation(InputCheck.optionalText(updates.get("designation"), "Designation", 100));

        if (updates.containsKey("salary") && updates.get("salary") != null && !updates.get("salary").toString().isBlank()) {
            BigDecimal salary = new BigDecimal(updates.get("salary").toString()); // "abc" gives a 400 "Invalid value"
            if (salary.signum() < 0) {
                throw new BadRequestException("Salary cannot be negative");
            }
            employee.setSalary(salary);
        }

        if (updates.containsKey("dateJoined") && updates.get("dateJoined") != null && !updates.get("dateJoined").toString().isBlank()) {
            LocalDate joined = LocalDate.parse(updates.get("dateJoined").toString());
            if (joined.isAfter(LocalDate.now())) {
                throw new BadRequestException("Date joined cannot be in the future");
            }
            employee.setDateJoined(joined);
        }

        if (updates.containsKey("departmentId") && updates.get("departmentId") != null && !updates.get("departmentId").toString().isBlank()) {
            Long deptId = Long.parseLong(updates.get("departmentId").toString());
            Department dept = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + deptId));
            employee.setDepartment(dept);
        }

        return employeeRepository.save(employee);
    }

    // HR removes an employee completely. Child rows go first, otherwise the
    // database foreign keys would block the delete. @Transactional = all or nothing.
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        User user = employee.getUser();

        attendanceRepository.deleteAllByEmployeeId(id);   // attendance history
        leaveRequestRepository.deleteAllByEmployeeId(id); // leave requests
        resetTokenRepository.deleteByUser(user);          // pending password reset links
        employeeRepository.delete(employee);              // profile
        userRepository.delete(user);                      // login account
    }

    @Transactional
    public Employee assignDepartment(Long employeeId, Long departmentId) {
        Employee employee = getEmployeeById(employeeId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));
        employee.setDepartment(department);
        return employeeRepository.save(employee);
    }
}
