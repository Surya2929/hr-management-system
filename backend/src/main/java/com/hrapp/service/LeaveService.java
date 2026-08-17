package com.hrapp.service;

import com.hrapp.entity.Employee;
import com.hrapp.entity.LeaveRequest;
import com.hrapp.entity.LeaveRequest.LeaveStatus;
import com.hrapp.entity.LeaveRequest.LeaveType;
import com.hrapp.repository.EmployeeRepository;
import com.hrapp.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveService {

    // Annual leave quota per type (simple fixed policy — adjust as needed)
    private static final int TOTAL_ANNUAL_QUOTA = 24; // total allowed days per year

    private final LeaveRequestRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    // ── EMPLOYEE: Apply for leave ─────────────────────────
    @Transactional
    public LeaveRequest applyLeave(Long userId, Map<String, Object> body) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));

        LocalDate fromDate = LocalDate.parse(body.get("fromDate").toString());
        LocalDate toDate   = LocalDate.parse(body.get("toDate").toString());

        // Validate dates
        if (toDate.isBefore(fromDate)) {
            throw new RuntimeException("toDate cannot be before fromDate");
        }
        if (fromDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot apply leave for a past date");
        }

        // Guard against overlapping approved/pending leaves
        if (leaveRepository.existsOverlappingLeave(employee.getId(), fromDate, toDate)) {
            throw new RuntimeException(
                    "You already have a leave request overlapping these dates");
        }

        LeaveType leaveType = LeaveType.valueOf(
                body.getOrDefault("leaveType", "CASUAL").toString().toUpperCase());

        String reason = body.containsKey("reason") ? body.get("reason").toString() : null;

        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .fromDate(fromDate)
                .toDate(toDate)
                .reason(reason)
                .status(LeaveStatus.PENDING)
                .build();

        return leaveRepository.save(request);
    }

    // ── EMPLOYEE: View own leave history ──────────────────
    public List<LeaveRequest> getMyLeaves(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));
        return leaveRepository.findByEmployeeIdOrderByAppliedAtDesc(employee.getId());
    }

    // ── EMPLOYEE: Leave balance summary ───────────────────
    public Map<String, Object> getLeaveBalance(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));

        int currentYear = LocalDate.now().getYear();
        long daysUsed = leaveRepository.countApprovedDaysInYear(
                employee.getId(), currentYear);

        Map<String, Object> balance = new HashMap<>();
        balance.put("year",          currentYear);
        balance.put("totalAllowed",  TOTAL_ANNUAL_QUOTA);
        balance.put("daysUsed",      daysUsed);
        balance.put("daysRemaining", TOTAL_ANNUAL_QUOTA - daysUsed);
        return balance;
    }

    // ── HR: View all pending leaves ───────────────────────
    public List<LeaveRequest> getPendingLeaves() {
        return leaveRepository.findByStatusOrderByAppliedAtAsc(LeaveStatus.PENDING);
    }

    // ── HR: View all leaves (any status) ─────────────────
    public List<LeaveRequest> getAllLeaves() {
        return leaveRepository.findAllByOrderByAppliedAtDesc();
    }

    // ── HR: Approve a leave request ───────────────────────
    @Transactional
    public LeaveRequest approveLeave(Long leaveId, String hrRemarks) {
        LeaveRequest leave = getLeaveById(leaveId);

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException(
                    "Only PENDING leaves can be approved. Current status: " + leave.getStatus());
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setHrRemarks(hrRemarks);
        leave.setReviewedAt(LocalDateTime.now());
        return leaveRepository.save(leave);
    }

    // ── HR: Reject a leave request ────────────────────────
    @Transactional
    public LeaveRequest rejectLeave(Long leaveId, String hrRemarks) {
        LeaveRequest leave = getLeaveById(leaveId);

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException(
                    "Only PENDING leaves can be rejected. Current status: " + leave.getStatus());
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setHrRemarks(hrRemarks);
        leave.setReviewedAt(LocalDateTime.now());
        return leaveRepository.save(leave);
    }

    // ── HR: View leaves for a specific employee ───────────
    public List<LeaveRequest> getLeavesForEmployee(Long employeeId) {
        // Verify employee exists
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employeeId));
        return leaveRepository.findByEmployeeIdOrderByAppliedAtDesc(employeeId);
    }

    // ── Private helper ────────────────────────────────────
    private LeaveRequest getLeaveById(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found: " + id));
    }
}
