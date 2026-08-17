package com.hrapp.controller;

import com.hrapp.entity.Attendance;
import com.hrapp.entity.User;
import com.hrapp.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * POST /api/attendance/mark
     * EMPLOYEE — mark own attendance for today (check-in)
     * Can also call again to record check-out time.
     *
     * Body (all optional — defaults applied):
     * {
     *   "status":   "PRESENT",          // PRESENT | ABSENT | HALF_DAY | ON_LEAVE
     *   "checkIn":  "09:15:00",         // HH:mm:ss — defaults to current time
     *   "checkOut": "18:30:00",         // optional, fill when leaving
     *   "remarks":  "Working from home"
     * }
     */
    @PostMapping("/mark")
    public ResponseEntity<Attendance> markMyAttendance(
            @AuthenticationPrincipal User currentUser,
            @RequestBody(required = false) Map<String, Object> body) {

        if (body == null) body = Map.of(); // allow empty body → defaults kick in
        return ResponseEntity.ok(
                attendanceService.markAttendance(currentUser.getId(), body));
    }

    /**
     * GET /api/attendance/me
     * EMPLOYEE — view own full attendance history (sorted newest first)
     */
    @GetMapping("/me")
    public ResponseEntity<List<Attendance>> getMyHistory(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                attendanceService.getMyAttendance(currentUser.getId()));
    }

    /**
     * GET /api/attendance/me/today
     * EMPLOYEE — check if attendance already marked today
     * Returns the record or null (200 with null body = not marked yet)
     */
    @GetMapping("/me/today")
    public ResponseEntity<Attendance> getMyTodayAttendance(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                attendanceService.getTodaysAttendance(currentUser.getId()));
    }

    /**
     * GET /api/attendance?date=2025-08-10
     * HR only — all employees' attendance on a specific date
     * Defaults to today if no date provided
     */
    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<Attendance>> getAttendanceByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }

    /**
     * GET /api/attendance/employee/{employeeId}?from=2025-07-01&to=2025-08-10
     * HR only — attendance history for a specific employee
     * Defaults to last 30 days if no range provided
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<Attendance>> getEmployeeHistory(
            @PathVariable Long employeeId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(
                attendanceService.getEmployeeAttendanceHistory(employeeId, from, to));
    }

    /**
     * POST /api/attendance/employee/{employeeId}
     * HR only — manually mark or override attendance for any employee on any date
     *
     * Body:
     * {
     *   "date":     "2025-08-09",    // optional — defaults to today
     *   "status":   "ABSENT",
     *   "remarks":  "No show"
     * }
     */
    @PostMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Attendance> markForEmployee(
            @PathVariable Long employeeId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(
                attendanceService.markAttendanceForEmployee(employeeId, body));
    }
}
