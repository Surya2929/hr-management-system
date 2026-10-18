package com.hrapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Attendance", description = "Check-in, check-out and attendance records")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "Mark my attendance for today (check-in)")
    @PostMapping("/mark")
    public ResponseEntity<Attendance> markMyAttendance(
            @AuthenticationPrincipal User currentUser,
            @RequestBody(required = false) Map<String, Object> body) {
        if (body == null) body = Map.of();
        return ResponseEntity.ok(attendanceService.markAttendance(currentUser.getId(), body));
    }

    @Operation(summary = "Check out for today - sets check-out time and returns working hours")
    @PostMapping("/checkout")
    public ResponseEntity<Attendance> checkOut(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(attendanceService.checkOut(currentUser.getId()));
    }

    @Operation(summary = "My attendance history")
    @GetMapping("/me")
    public ResponseEntity<List<Attendance>> getMyHistory(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(attendanceService.getMyAttendance(currentUser.getId()));
    }

    @Operation(summary = "My attendance record for today (or empty)")
    @GetMapping("/me/today")
    public ResponseEntity<Attendance> getMyTodayAttendance(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(attendanceService.getTodaysAttendance(currentUser.getId()));
    }

    @Operation(summary = "All employees' attendance for a date (HR)")
    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<Attendance>> getAttendanceByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }

    @Operation(summary = "One employee's attendance between two dates (HR)")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<Attendance>> getEmployeeHistory(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendanceHistory(employeeId, from, to));
    }

    @Operation(summary = "Mark or edit attendance for an employee (HR)")
    @PostMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Attendance> markForEmployee(
            @PathVariable Long employeeId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(attendanceService.markAttendanceForEmployee(employeeId, body));
    }
}
