package com.hrapp.controller;

import com.hrapp.entity.Employee;
import com.hrapp.entity.User;
import com.hrapp.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * GET /api/employees
     * HR only — list all employees
     */
    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /**
     * GET /api/employees/{id}
     * HR only — get a specific employee by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    /**
     * GET /api/employees/me
     * EMPLOYEE — view their own profile
     * Uses @AuthenticationPrincipal to get the logged-in User entity
     */
    @GetMapping("/me")
    public ResponseEntity<Employee> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(employeeService.getEmployeeByUserId(currentUser.getId()));
    }

    /**
     * PUT /api/employees/{id}
     * HR only — update any field (partial update via JSON map)
     *
     * Accepted fields: firstName, lastName, phone, designation,
     *                  salary, dateJoined, departmentId
     *
     * Example body:
     * {
     *   "designation": "Senior Engineer",
     *   "salary": 75000,
     *   "departmentId": 2
     * }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, updates));
    }

    /**
     * PUT /api/employees/{id}/department/{departmentId}
     * HR only — quickly assign or change an employee's department
     */
    @PutMapping("/{id}/department/{departmentId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Employee> assignDepartment(@PathVariable Long id,
                                                     @PathVariable Long departmentId) {
        return ResponseEntity.ok(employeeService.assignDepartment(id, departmentId));
    }

    /**
     * DELETE /api/employees/{id}
     * HR only — deletes the employee and their user account
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }
}
