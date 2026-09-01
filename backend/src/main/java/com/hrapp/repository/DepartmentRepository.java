package com.hrapp.repository;
import com.hrapp.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DepartmentRepository extends JpaRepository<Department,Long> {
    boolean existsByName(String name);
}
