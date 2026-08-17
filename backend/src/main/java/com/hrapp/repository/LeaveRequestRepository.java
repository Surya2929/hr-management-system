package com.hrapp.repository;

import com.hrapp.entity.LeaveRequest;
import com.hrapp.entity.LeaveRequest.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // Employee: own leave history, newest first
    List<LeaveRequest> findByEmployeeIdOrderByAppliedAtDesc(Long employeeId);

    // HR: all leaves filtered by status (e.g. PENDING)
    List<LeaveRequest> findByStatusOrderByAppliedAtAsc(LeaveStatus status);

    // HR: all leaves regardless of status, newest first
    List<LeaveRequest> findAllByOrderByAppliedAtDesc();

    // Leave balance — count approved days for an employee in a given year
    // Used to calculate how many days have been taken
    @Query("""
        SELECT COALESCE(SUM(DATEDIFF(lr.toDate, lr.fromDate) + 1), 0)
        FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
          AND lr.status = 'APPROVED'
          AND YEAR(lr.fromDate) = :year
    """)
    Long countApprovedDaysInYear(@Param("employeeId") Long employeeId,
                                  @Param("year") int year);

    // Check overlapping leaves — prevent double-booking
    @Query("""
        SELECT COUNT(lr) > 0
        FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
          AND lr.status <> 'REJECTED'
          AND lr.fromDate <= :toDate
          AND lr.toDate   >= :fromDate
    """)
    boolean existsOverlappingLeave(@Param("employeeId") Long employeeId,
                                    @Param("fromDate")   LocalDate fromDate,
                                    @Param("toDate")     LocalDate toDate);
}
