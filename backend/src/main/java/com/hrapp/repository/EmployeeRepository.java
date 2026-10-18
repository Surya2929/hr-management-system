package com.hrapp.repository;

import com.hrapp.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    Optional<Employee> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    // Case-insensitive search; a null parameter means "no filter"
    @Query("SELECT e FROM Employee e WHERE " +
           "(:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR LOWER(e.designation) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:departmentId IS NULL OR e.department.id = :departmentId)")
    Page<Employee> search(@Param("search") String search,
                          @Param("departmentId") Long departmentId,
                          Pageable pageable);

    // Dashboard: employees per department (GROUP BY in the database, no Java loops)
    @Query("SELECT COALESCE(d.name, 'Unassigned'), COUNT(e) FROM Employee e " +
           "LEFT JOIN e.department d GROUP BY d.name ORDER BY COUNT(e) DESC")
    List<Object[]> countByDepartment();
}
