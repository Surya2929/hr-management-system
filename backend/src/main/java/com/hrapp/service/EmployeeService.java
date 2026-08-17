package com.hrapp.service;

import com.hrapp.entity.Department;
import com.hrapp.entity.Employee;
import com.hrapp.entity.User;
import com.hrapp.repository.DepartmentRepository;
import com.hrapp.repository.EmployeeRepository;
import com.hrapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    // ── List all employees (HR view) ──────────────────────
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // ── Get employee by ID ────────────────────────────────
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    // ── Get employee by logged-in userId (for /me endpoint) ──
    public Employee getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found for user: " + userId));
    }

    // ── Update employee profile (HR) ──────────────────────
    // Uses a Map so HR can update any subset of fields
    @Transactional
    public Employee updateEmployee(Long id, Map<String, Object> updates) {
        Employee employee = getEmployeeById(id);

        if (updates.containsKey("firstName"))
            employee.setFirstName((String) updates.get("firstName"));

        if (updates.containsKey("lastName"))
            employee.setLastName((String) updates.get("lastName"));

        if (updates.containsKey("phone"))
            employee.setPhone((String) updates.get("phone"));

        if (updates.containsKey("designation"))
            employee.setDesignation((String) updates.get("designation"));

        if (updates.containsKey("salary"))
            employee.setSalary(new BigDecimal(updates.get("salary").toString()));

        if (updates.containsKey("dateJoined"))
            employee.setDateJoined(LocalDate.parse(updates.get("dateJoined").toString()));

        if (updates.containsKey("departmentId")) {
            Long deptId = Long.parseLong(updates.get("departmentId").toString());
            Department dept = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new RuntimeException("Department not found: " + deptId));
            employee.setDepartment(dept);
        }

        return employeeRepository.save(employee);
    }

    // ── Delete employee + associated user account ─────────
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        Long userId = employee.getUser().getId();

        // Deleting the User cascades and removes the Employee too (CascadeType via FK)
        // But we explicitly delete employee first, then user to be safe
        employeeRepository.deleteById(id);
        userRepository.deleteById(userId);
    }

    // ── Assign / change department ────────────────────────
    @Transactional
    public Employee assignDepartment(Long employeeId, Long departmentId) {
        Employee employee = getEmployeeById(employeeId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));
        employee.setDepartment(department);
        return employeeRepository.save(employee);
    }
}
