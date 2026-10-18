package com.hrapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hrapp.entity.LeaveRequest;
import com.hrapp.entity.User;
import com.hrapp.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Leave", description = "Leave requests and approvals")
@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @Operation(summary = "Apply for leave")
    @PostMapping("/apply")
    public ResponseEntity<LeaveRequest> applyLeave(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leaveService.applyLeave(currentUser.getId(), body));
    }

    @Operation(summary = "My leave requests")
    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequest>> getMyLeaves(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(leaveService.getMyLeaves(currentUser.getId()));
    }

    @Operation(summary = "My leave balance for this year")
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getLeaveBalance(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(currentUser.getId()));
    }

    @Operation(summary = "All pending leave requests (HR)")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<LeaveRequest>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    @Operation(summary = "All leave requests (HR)")
    @GetMapping("/all")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<LeaveRequest>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @Operation(summary = "Leave requests of one employee (HR)")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<LeaveRequest>> getLeavesForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeavesForEmployee(employeeId));
    }

    @Operation(summary = "Approve a pending leave (HR)")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<LeaveRequest> approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String remarks = (body != null) ? body.getOrDefault("hrRemarks", null) : null;
        return ResponseEntity.ok(leaveService.approveLeave(id, remarks));
    }

    @Operation(summary = "Reject a pending leave (HR)")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<LeaveRequest> rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String remarks = (body != null) ? body.getOrDefault("hrRemarks", null) : null;
        return ResponseEntity.ok(leaveService.rejectLeave(id, remarks));
    }
}
