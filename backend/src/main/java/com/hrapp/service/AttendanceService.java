package com.hrapp.service;

import com.hrapp.exception.ResourceNotFoundException;
import com.hrapp.entity.Attendance;
import com.hrapp.entity.Employee;
import com.hrapp.repository.AttendanceRepository;
import com.hrapp.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    // Check-in after this time counts as "late" (application.properties, default 09:30)
    @Value("${app.attendance.late-after:09:30}")
    private String lateAfter;

    @Transactional
    public Attendance markAttendance(Long userId, Map<String, Object> body) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

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

        return addTimeInfo(attendanceRepository.save(attendance));
    }

    // Logged-in employee checks out for today
    @Transactional
    public Attendance checkOut(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employee.getId(), LocalDate.now())
                .orElseThrow(() -> new IllegalArgumentException("You have not checked in today"));

        if (attendance.getCheckIn() == null) {
            throw new IllegalArgumentException("You have not checked in today");
        }
        if (attendance.getCheckOut() != null) {
            throw new IllegalArgumentException("You have already checked out today");
        }

        attendance.setCheckOut(LocalTime.now().withNano(0));
        return addTimeInfo(attendanceRepository.save(attendance));
    }

    @Transactional
    public Attendance markAttendanceForEmployee(Long employeeId, Map<String, Object> body) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));

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

        return addTimeInfo(attendanceRepository.save(attendance));
    }

    public List<Attendance> getMyAttendance(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));
        return addTimeInfo(attendanceRepository.findByEmployeeIdOrderByDateDesc(employee.getId()));
    }

    public Attendance getTodaysAttendance(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));
        return attendanceRepository
                .findByEmployeeIdAndDate(employee.getId(), LocalDate.now())
                .map(this::addTimeInfo)
                .orElse(null);
    }

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return addTimeInfo(attendanceRepository.findByDateOrderByEmployeeFirstNameAsc(date));
    }

    public List<Attendance> getEmployeeAttendanceHistory(Long employeeId, LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.now().minusMonths(1);
        if (to == null) to = LocalDate.now();
        return addTimeInfo(attendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateDesc(employeeId, from, to));
    }

    // Fills the calculated (not stored) fields: workingHours and late
    private Attendance addTimeInfo(Attendance a) {
        if (a.getCheckIn() != null && a.getCheckOut() != null && a.getCheckOut().isAfter(a.getCheckIn())) {
            long minutes = Duration.between(a.getCheckIn(), a.getCheckOut()).toMinutes();
            a.setWorkingHours(Math.round(minutes / 60.0 * 100) / 100.0); // 2 decimal places
        }
        a.setLate(a.getCheckIn() != null && a.getCheckIn().isAfter(LocalTime.parse(lateAfter)));
        return a;
    }

    private List<Attendance> addTimeInfo(List<Attendance> list) {
        list.forEach(this::addTimeInfo);
        return list;
    }
}
