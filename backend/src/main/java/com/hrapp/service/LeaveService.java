package com.hrapp.service;

import com.hrapp.exception.ResourceNotFoundException;
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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private static final int TOTAL_ANNUAL_QUOTA = 24;

    private final LeaveRequestRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveRequest applyLeave(Long userId, Map<String, Object> body) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        LocalDate fromDate = LocalDate.parse(body.get("fromDate").toString());
        LocalDate toDate   = LocalDate.parse(body.get("toDate").toString());

        if (toDate.isBefore(fromDate)) {
            throw new RuntimeException("toDate cannot be before fromDate");
        }
        if (fromDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot apply leave for a past date");
        }

        if (leaveRepository.existsOverlappingLeave(employee.getId(), fromDate, toDate)) {
            throw new RuntimeException("You already have a leave request overlapping these dates");
        }

        LeaveType leaveType = LeaveType.valueOf(
                body.getOrDefault("leaveType", "CASUAL").toString().toUpperCase());

        // Enforce the yearly quota: approved + already pending + this request must fit in 24 days
        if (leaveType != LeaveType.UNPAID) {
            int year = fromDate.getYear();
            long requested = daysBetween(fromDate, toDate);
            long approved  = paidDaysInYear(employee.getId(), LeaveStatus.APPROVED, year);
            long pending   = paidDaysInYear(employee.getId(), LeaveStatus.PENDING, year);
            long available = TOTAL_ANNUAL_QUOTA - approved - pending;
            if (requested > available) {
                throw new IllegalArgumentException("Not enough leave balance for " + year + ": you asked for "
                        + requested + " day(s) but only " + Math.max(0, available) + " are available"
                        + (pending > 0 ? " (" + pending + " day(s) already pending approval)" : "")
                        + ". Choose Unpaid Leave for extra days.");
            }
        }

        String reason = body.containsKey("reason") && body.get("reason") != null ? body.get("reason").toString() : null;

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

    public List<LeaveRequest> getMyLeaves(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));
        return leaveRepository.findByEmployeeIdOrderByAppliedAtDesc(employee.getId());
    }

    public Map<String, Object> getLeaveBalance(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        int currentYear = LocalDate.now().getYear();
        long daysUsed    = paidDaysInYear(employee.getId(), LeaveStatus.APPROVED, currentYear);
        long daysPending = paidDaysInYear(employee.getId(), LeaveStatus.PENDING, currentYear);

        Map<String, Object> balance = new HashMap<>();
        balance.put("year", currentYear);
        balance.put("totalAllowed", TOTAL_ANNUAL_QUOTA);
        balance.put("daysUsed", daysUsed);
        balance.put("daysPending", daysPending);
        balance.put("daysRemaining", Math.max(0, TOTAL_ANNUAL_QUOTA - daysUsed));
        return balance;
    }

    public List<LeaveRequest> getPendingLeaves() {
        return leaveRepository.findByStatusOrderByAppliedAtAsc(LeaveStatus.PENDING);
    }

    public List<LeaveRequest> getAllLeaves() {
        return leaveRepository.findAllByOrderByAppliedAtDesc();
    }

    @Transactional
    public LeaveRequest approveLeave(Long leaveId, String hrRemarks) {
        LeaveRequest leave = getLeaveById(leaveId);

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Only PENDING leaves can be approved. Current status: " + leave.getStatus());
        }

        // Re-check the quota at approval time, so HR can never approve past 24 paid days
        if (leave.getLeaveType() != LeaveType.UNPAID) {
            int year = leave.getFromDate().getYear();
            long approved  = paidDaysInYear(leave.getEmployee().getId(), LeaveStatus.APPROVED, year);
            long requested = daysBetween(leave.getFromDate(), leave.getToDate());
            if (approved + requested > TOTAL_ANNUAL_QUOTA) {
                throw new IllegalArgumentException("Cannot approve: the employee has only "
                        + Math.max(0, TOTAL_ANNUAL_QUOTA - approved) + " paid leave day(s) left in " + year
                        + " but this request is for " + requested + ". Reject it or ask for Unpaid Leave.");
            }
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setHrRemarks(hrRemarks);
        leave.setReviewedAt(LocalDateTime.now());
        return leaveRepository.save(leave);
    }

    @Transactional
    public LeaveRequest rejectLeave(Long leaveId, String hrRemarks) {
        LeaveRequest leave = getLeaveById(leaveId);

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Only PENDING leaves can be rejected. Current status: " + leave.getStatus());
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setHrRemarks(hrRemarks);
        leave.setReviewedAt(LocalDateTime.now());
        return leaveRepository.save(leave);
    }

    public List<LeaveRequest> getLeavesForEmployee(Long employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
        return leaveRepository.findByEmployeeIdOrderByAppliedAtDesc(employeeId);
    }

    private LeaveRequest getLeaveById(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + id));
    }

    // Inclusive day count: 1st to 3rd = 3 days (same rule as the database query)
    private long daysBetween(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to) + 1;
    }

    // Total PAID leave days (UNPAID not counted) for one employee, status and year.
    // A leave counts in the year it starts. An employee has only a few leaves a year, so summing in Java is cheap.
    private long paidDaysInYear(Long employeeId, LeaveStatus status, int year) {
        return leaveRepository.findByEmployeeIdAndStatusAndLeaveTypeNot(employeeId, status, LeaveType.UNPAID)
                .stream()
                .filter(l -> l.getFromDate().getYear() == year)
                .mapToLong(l -> daysBetween(l.getFromDate(), l.getToDate()))
                .sum();
    }
}
