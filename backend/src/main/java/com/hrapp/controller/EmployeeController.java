package com.hrapp.controller;

import com.hrapp.dto.CreateEmployeeRequest;
import com.hrapp.dto.CreateEmployeeResponse;
import com.hrapp.entity.Employee;
import com.hrapp.entity.User;
import com.hrapp.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Employees", description = "Employee records (mostly HR only)")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(summary = "List employees with search, department filter and pagination (HR)")
    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // size is capped at 100 so nobody can ask for the whole table at once
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("firstName").and(Sort.by("lastName")));
        return ResponseEntity.ok(employeeService.searchEmployees(search, departmentId, pageable));
    }

    @Operation(summary = "Add a new hire - returns a one-time temporary password (HR)")
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<CreateEmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    @Operation(summary = "Issue a new temporary password for an employee (HR)")
    @PostMapping("/{id}/reset-temp-password")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Map<String, String>> resetTemporaryPassword(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("temporaryPassword", employeeService.resetTemporaryPassword(id)));
    }

    @Operation(summary = "One employee by id (HR)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(summary = "My own employee profile")
    @GetMapping("/me")
    public ResponseEntity<Employee> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(employeeService.getEmployeeByUserId(currentUser.getId()));
    }

    @Operation(summary = "Update employee details (HR)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, updates));
    }

    @Operation(summary = "Move an employee to a department (HR)")
    @PutMapping("/{id}/department/{departmentId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Employee> assignDepartment(@PathVariable Long id, @PathVariable Long departmentId) {
        return ResponseEntity.ok(employeeService.assignDepartment(id, departmentId));
    }

    @Operation(summary = "Delete an employee and their login (HR)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully");
    }
}
