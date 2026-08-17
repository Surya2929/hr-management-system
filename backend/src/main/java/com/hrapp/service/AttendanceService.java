package com.hrapp.service;

import com.hrapp.entity.Attendance;
import com.hrapp.entity.Employee;
import com.hrapp.repository.AttendanceRepository;
import com.hrapp.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    // ── Mark attendance for today (called by EMPLOYEE) ────
    // If already marked today → updates the record (allows check-out update)
    @Transactional
    public Attendance markAttendance(Long userId, Map<String, Object> body) {
        // Resolve Employee from logged-in userId
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));

        LocalDate today = LocalDate.now();

        // Check if already marked today
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employee.getId(), today)
                .orElse(Attendance.builder()
                        .employee(employee)
                        .date(today)
                        .build());

        // Set status (required)
        if (body.containsKey("status")) {
            attendance.setStatus(
                    Attendance.AttendanceStatus.valueOf(
                            body.get("status").toString().toUpperCase()));
        } else {
            // Default to PRESENT if not specified
            attendance.setStatus(Attendance.AttendanceStatus.PRESENT);
        }

        // Set check-in time
        if (body.containsKey("checkIn")) {
            attendance.setCheckIn(LocalTime.parse(body.get("checkIn").toString()));
        } else if (attendance.getCheckIn() == null) {
            attendance.setCheckIn(LocalTime.now());
        }

        // Set check-out time (optional — employee may call again later)
        if (body.containsKey("checkOut")) {
            attendance.setCheckOut(LocalTime.parse(body.get("checkOut").toString()));
        }

        // Optional remarks
        if (body.containsKey("remarks")) {
            attendance.setRemarks(body.get("remarks").toString());
        }

        return attendanceRepository.save(attendance);
    }

    // ── HR: manually mark/override attendance for any employee ──
    @Transactional
    public Attendance markAttendanceForEmployee(Long employeeId, Map<String, Object> body) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        // Parse date from body or default to today
        LocalDate date = body.containsKey("date")
                ? LocalDate.parse(body.get("date").toString())
                : LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employeeId, date)
                .orElse(Attendance.builder()
                        .employee(employee)
                        .date(date)
                        .build());

        if (body.containsKey("status")) {
            attendance.setStatus(
                    Attendance.AttendanceStatus.valueOf(
                            body.get("status").toString().toUpperCase()));
        }
        if (body.containsKey("checkIn")) {
            attendance.setCheckIn(LocalTime.parse(body.get("checkIn").toString()));
        }
        if (body.containsKey("checkOut")) {
            attendance.setCheckOut(LocalTime.parse(body.get("checkOut").toString()));
        }
        if (body.containsKey("remarks")) {
            attendance.setRemarks(body.get("remarks").toString());
        }

        return attendanceRepository.save(attendance);
    }

    // ── Employee: view own attendance history ─────────────
    public List<Attendance> getMyAttendance(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));
        return attendanceRepository.findByEmployeeIdOrderByDateDesc(employee.getId());
    }

    // ── Employee: today's attendance record ───────────────
    public Attendance getTodaysAttendance(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));
        return attendanceRepository
                .findByEmployeeIdAndDate(employee.getId(), LocalDate.now())
                .orElse(null); // null → not yet marked today
    }

    // ── HR: all employees' attendance on a specific date ──
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDateOrderByEmployeeFirstNameAsc(date);
    }

    // ── HR: attendance history for one employee (optional date range) ──
    public List<Attendance> getEmployeeAttendanceHistory(Long employeeId,
                                                          LocalDate from,
                                                          LocalDate to) {
        if (from == null) from = LocalDate.now().minusMonths(1);
        if (to   == null) to   = LocalDate.now();
        return attendanceRepository
                .findByEmployeeIdAndDateBetweenOrderByDateDesc(employeeId, from, to);
    }
}
