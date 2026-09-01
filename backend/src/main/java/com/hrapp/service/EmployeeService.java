package com.hrapp.service;

import com.hrapp.entity.Department;
import com.hrapp.entity.Employee;
import com.hrapp.repository.DepartmentRepository;
import com.hrapp.repository.EmployeeRepository;
import com.hrapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    public Employee getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found for user: " + userId));
    }

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

        if (updates.containsKey("salary") && updates.get("salary") != null && !updates.get("salary").toString().isBlank())
            employee.setSalary(new BigDecimal(updates.get("salary").toString()));

        if (updates.containsKey("dateJoined") && updates.get("dateJoined") != null && !updates.get("dateJoined").toString().isBlank())
            employee.setDateJoined(LocalDate.parse(updates.get("dateJoined").toString()));

        if (updates.containsKey("departmentId") && updates.get("departmentId") != null && !updates.get("departmentId").toString().isBlank()) {
            Long deptId = Long.parseLong(updates.get("departmentId").toString());
            Department dept = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new RuntimeException("Department not found: " + deptId));
            employee.setDepartment(dept);
        }

        return employeeRepository.save(employee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        Long userId = employee.getUser().getId();
        employeeRepository.deleteById(id);
        userRepository.deleteById(userId);
    }

    @Transactional
    public Employee assignDepartment(Long employeeId, Long departmentId) {
        Employee employee = getEmployeeById(employeeId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));
        employee.setDepartment(department);
        return employeeRepository.save(employee);
    }
}
