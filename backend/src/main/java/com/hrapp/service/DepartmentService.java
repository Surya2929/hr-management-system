package com.hrapp.service;

import com.hrapp.util.InputCheck;
import com.hrapp.exception.BadRequestException;
import com.hrapp.exception.ResourceNotFoundException;
import com.hrapp.entity.Department;
import com.hrapp.repository.DepartmentRepository;
import com.hrapp.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    public Department createDepartment(Department department) {
        String name = InputCheck.requiredText(department.getName(), "Department name", 100);
        // Case-insensitive: "Sales" and "sales" count as the same department
        if (departmentRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new BadRequestException("Department already exists: " + name);
        }
        department.setName(name);
        department.setDescription(InputCheck.optionalText(department.getDescription(), "Description", 255));
        return departmentRepository.save(department);
    }

    public Department updateDepartment(Long id, Department updated) {
        Department existing = getDepartmentById(id);
        String name = InputCheck.requiredText(updated.getName(), "Department name", 100);
        // Is another department (not this one) already using the name?
        departmentRepository.findByNameIgnoreCase(name)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new BadRequestException("Department already exists: " + name); });
        existing.setName(name);
        existing.setDescription(InputCheck.optionalText(updated.getDescription(), "Description", 255));
        return departmentRepository.save(existing);
    }

    // Employees of this department are kept - their department just becomes empty (null).
    // @Transactional: both steps succeed together or nothing changes.
    @Transactional
    public void deleteDepartment(Long id) {
        getDepartmentById(id);
        employeeRepository.clearDepartment(id); // 1. unlink employees (otherwise the foreign key blocks the delete)
        departmentRepository.deleteById(id);    // 2. delete the department
    }
}
