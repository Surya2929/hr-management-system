package com.hrapp.repository;
import java.util.Optional;
import com.hrapp.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DepartmentRepository extends JpaRepository<Department,Long> {
    boolean existsByName(String name);
    Optional<Department> findByNameIgnoreCase(String name);
}
