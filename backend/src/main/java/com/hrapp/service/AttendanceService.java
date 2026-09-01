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

    @Transactional
    public Attendance markAttendance(Long userId, Map<String, Object> body) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));

        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employee.getId(), today)
                .orElse(Attendance.builder()
                        .employee(employee)
                        .date(today)
                        .build());

        if (body.containsKey("status") && body.get("status") != null) {
            attendance.setStatus(
                    Attendance.AttendanceStatus.valueOf(
                            body.get("status").toString().toUpperCase()));
        } else if (attendance.getStatus() == null) {
            attendance.setStatus(Attendance.AttendanceStatus.PRESENT);
        }

        if (body.containsKey("checkIn") && body.get("checkIn") != null && !body.get("checkIn").toString().isBlank()) {
            attendance.setCheckIn(LocalTime.parse(body.get("checkIn").toString()));
        } else if (attendance.getCheckIn() == null) {
            attendance.setCheckIn(LocalTime.now());
        }

        if (body.containsKey("checkOut") && body.get("checkOut") != null && !body.get("checkOut").toString().isBlank()) {
            attendance.setCheckOut(LocalTime.parse(body.get("checkOut").toString()));
        }

        if (body.containsKey("remarks")) {
            attendance.setRemarks(body.get("remarks") != null ? body.get("remarks").toString() : null);
        }

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance markAttendanceForEmployee(Long employeeId, Map<String, Object> body) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));

        LocalDate date = body.containsKey("date") && body.get("date") != null && !body.get("date").toString().isBlank()
                ? LocalDate.parse(body.get("date").toString())
                : LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employeeId, date)
                .orElse(Attendance.builder()
                        .employee(employee)
                        .date(date)
                        .build());

        if (body.containsKey("status") && body.get("status") != null) {
            attendance.setStatus(
                    Attendance.AttendanceStatus.valueOf(
                            body.get("status").toString().toUpperCase()));
        }
        if (body.containsKey("checkIn") && body.get("checkIn") != null && !body.get("checkIn").toString().isBlank()) {
            attendance.setCheckIn(LocalTime.parse(body.get("checkIn").toString()));
        }
        if (body.containsKey("checkOut") && body.get("checkOut") != null && !body.get("checkOut").toString().isBlank()) {
            attendance.setCheckOut(LocalTime.parse(body.get("checkOut").toString()));
        }
        if (body.containsKey("remarks")) {
            attendance.setRemarks(body.get("remarks") != null ? body.get("remarks").toString() : null);
        }

        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getMyAttendance(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));
        return attendanceRepository.findByEmployeeIdOrderByDateDesc(employee.getId());
    }

    public Attendance getTodaysAttendance(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));
        return attendanceRepository
                .findByEmployeeIdAndDate(employee.getId(), LocalDate.now())
                .orElse(null);
    }

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDateOrderByEmployeeFirstNameAsc(date);
    }

    public List<Attendance> getEmployeeAttendanceHistory(Long employeeId, LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.now().minusMonths(1);
        if (to == null) to = LocalDate.now();
        return attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(employeeId, from, to);
    }
}
