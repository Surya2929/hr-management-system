package com.hrapp.service;

import com.hrapp.entity.Department;
import com.hrapp.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // ── Get all departments ───────────────────────────────
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // ── Get by ID ─────────────────────────────────────────
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
    }

    // ── Create department ─────────────────────────────────
    public Department createDepartment(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new RuntimeException("Department already exists: " + department.getName());
        }
        return departmentRepository.save(department);
    }

    // ── Update department ─────────────────────────────────
    public Department updateDepartment(Long id, Department updated) {
        Department existing = getDepartmentById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return departmentRepository.save(existing);
    }

    // ── Delete department ─────────────────────────────────
    public void deleteDepartment(Long id) {
        getDepartmentById(id); // throws if not found
        departmentRepository.deleteById(id);
    }
}
