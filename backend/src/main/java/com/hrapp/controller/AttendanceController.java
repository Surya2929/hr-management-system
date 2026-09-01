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

    @PostMapping("/mark")
    public ResponseEntity<Attendance> markMyAttendance(
            @AuthenticationPrincipal User currentUser,
            @RequestBody(required = false) Map<String, Object> body) {
        if (body == null) body = Map.of();
        return ResponseEntity.ok(attendanceService.markAttendance(currentUser.getId(), body));
    }

    @GetMapping("/me")
    public ResponseEntity<List<Attendance>> getMyHistory(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(attendanceService.getMyAttendance(currentUser.getId()));
    }

    @GetMapping("/me/today")
    public ResponseEntity<Attendance> getMyTodayAttendance(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(attendanceService.getTodaysAttendance(currentUser.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<Attendance>> getAttendanceByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<Attendance>> getEmployeeHistory(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendanceHistory(employeeId, from, to));
    }

    @PostMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Attendance> markForEmployee(
            @PathVariable Long employeeId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(attendanceService.markAttendanceForEmployee(employeeId, body));
    }
}
