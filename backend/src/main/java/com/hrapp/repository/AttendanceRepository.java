package com.hrapp.repository;

import com.hrapp.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Employee's full attendance history
    List<Attendance> findByEmployeeIdOrderByDateDesc(Long employeeId);

    // Check if attendance already marked for a specific day
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    // HR: view all employee attendance on a given date
    List<Attendance> findByDateOrderByEmployeeFirstNameAsc(LocalDate date);

    // HR: attendance for a specific employee within a date range
    List<Attendance> findByEmployeeIdAndDateBetweenOrderByDateDesc(
            Long employeeId, LocalDate from, LocalDate to);
}
