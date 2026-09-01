package com.hrapp.repository;
import com.hrapp.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface AttendanceRepository extends JpaRepository<Attendance,Long> {
    List<Attendance> findByEmployeeIdOrderByDateDesc(Long employeeId);
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    List<Attendance> findByDateOrderByEmployeeFirstNameAsc(LocalDate date);
    List<Attendance> findByEmployeeIdAndDateBetweenOrderByDateDesc(Long employeeId, LocalDate from, LocalDate to);
}
