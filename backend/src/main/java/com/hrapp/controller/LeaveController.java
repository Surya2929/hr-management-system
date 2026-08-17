package com.hrapp.controller;

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

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    // ════════════════════════════════════════════════════
    //  EMPLOYEE ENDPOINTS
    // ════════════════════════════════════════════════════

    /**
     * POST /api/leave/apply
     * EMPLOYEE — submit a leave request
     *
     * Body:
     * {
     *   "fromDate":  "2025-08-15",
     *   "toDate":    "2025-08-17",
     *   "leaveType": "CASUAL",       // SICK | CASUAL | EARNED | UNPAID
     *   "reason":    "Personal work"
     * }
     */
    @PostMapping("/apply")
    public ResponseEntity<LeaveRequest> applyLeave(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leaveService.applyLeave(currentUser.getId(), body));
    }

    /**
     * GET /api/leave/my
     * EMPLOYEE — view own leave history (all statuses)
     */
    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequest>> getMyLeaves(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(leaveService.getMyLeaves(currentUser.getId()));
    }

    /**
     * GET /api/leave/balance
     * EMPLOYEE — view leave balance for the current year
     *
     * Response:
     * {
     *   "year": 2025,
     *   "totalAllowed": 24,
     *   "daysUsed": 5,
     *   "daysRemaining": 19
     * }
     */
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getLeaveBalance(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(leaveService.getLeaveBalance(currentUser.getId()));
    }

    // ════════════════════════════════════════════════════
    //  HR ENDPOINTS
    // ════════════════════════════════════════════════════

    /**
     * GET /api/leave/pending
     * HR only — all PENDING leave requests sorted oldest first
     * (oldest first so HR reviews in order they were applied)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<LeaveRequest>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    /**
     * GET /api/leave/all
     * HR only — all leave requests regardless of status
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<LeaveRequest>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    /**
     * GET /api/leave/employee/{employeeId}
     * HR only — all leaves for a specific employee
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<LeaveRequest>> getLeavesForEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeavesForEmployee(employeeId));
    }

    /**
     * PUT /api/leave/{id}/approve
     * HR only — approve a PENDING leave request
     *
     * Body (optional):
     * { "hrRemarks": "Approved. Enjoy your break." }
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<LeaveRequest> approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String remarks = (body != null) ? body.getOrDefault("hrRemarks", null) : null;
        return ResponseEntity.ok(leaveService.approveLeave(id, remarks));
    }

    /**
     * PUT /api/leave/{id}/reject
     * HR only — reject a PENDING leave request
     *
     * Body (optional):
     * { "hrRemarks": "Rejected due to project deadline." }
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<LeaveRequest> rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String remarks = (body != null) ? body.getOrDefault("hrRemarks", null) : null;
        return ResponseEntity.ok(leaveService.rejectLeave(id, remarks));
    }
}
