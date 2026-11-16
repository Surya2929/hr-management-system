package com.hrapp.service;

import com.hrapp.util.InputCheck;
import com.hrapp.exception.BadRequestException;
import com.hrapp.exception.ResourceNotFoundException;
import com.hrapp.entity.Attendance;
import com.hrapp.entity.Employee;
import com.hrapp.entity.LeaveRequest;
import com.hrapp.entity.LeaveRequest.LeaveStatus;
import com.hrapp.entity.LeaveRequest.LeaveType;
import com.hrapp.repository.AttendanceRepository;
import com.hrapp.repository.EmployeeRepository;
import com.hrapp.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private static final int TOTAL_ANNUAL_QUOTA = 24;

    private final LeaveRequestRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public LeaveRequest applyLeave(Long userId, Map<String, Object> body) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));

        // Dates are required (a missing date used to crash with a NullPointerException)
        LocalDate fromDate = LocalDate.parse(InputCheck.requiredText(body.get("fromDate"), "From date", 10));
        LocalDate toDate   = LocalDate.parse(InputCheck.requiredText(body.get("toDate"), "To date", 10));

        if (toDate.isBefore(fromDate)) {
            throw new BadRequestException("toDate cannot be before fromDate");
        }
        if (fromDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot apply leave for a past date");
        }
        // Weekends are not counted as leave, so a weekend-only request makes no sense
        if (workingDays(fromDate, toDate) == 0) {
            throw new BadRequestException("The selected dates fall on a weekend - no leave is needed");
        }

        if (leaveRepository.existsOverlappingLeave(employee.getId(), fromDate, toDate)) {
            throw new BadRequestException("You already have a leave request overlapping these dates");
        }

        LeaveType leaveType = LeaveType.valueOf(
                body.getOrDefault("leaveType", "CASUAL").toString().toUpperCase());

        // Enforce the yearly quota for EACH year the leave touches
        // (30 Dec - 2 Jan uses some of this year's and some of next year's quota)
        if (leaveType != LeaveType.UNPAID) {
            for (int year = fromDate.getYear(); year <= toDate.getYear(); year++) {
                long requested = workingDaysInYear(fromDate, toDate, year);
                long approved  = paidDaysInYear(employee.getId(), LeaveStatus.APPROVED, year);
                long pending   = paidDaysInYear(employee.getId(), LeaveStatus.PENDING, year);
                long available = TOTAL_ANNUAL_QUOTA - approved - pending;
                if (requested > available) {
                    throw new BadRequestException("Not enough leave balance for " + year + ": you asked for "
                            + requested + " working day(s) but only " + Math.max(0, available) + " are available"
                            + (pending > 0 ? " (" + pending + " day(s) already pending approval)" : "")
                            + ". Choose Unpaid Leave for extra days.");
                }
            }
        }

        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .fromDate(fromDate)
                .toDate(toDate)
                .reason(InputCheck.optionalText(body.get("reason"), "Reason", 2_000))
                .status(LeaveStatus.PENDING)
                .build();

        return leaveRepository.save(request);
    }

    // Employee withdraws their own leave while it is still PENDING.
    // The row is deleted, so the days go straight back to the balance.
    @Transactional
    public void cancelLeave(Long userId, Long leaveId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));
        LeaveRequest leave = getLeaveById(leaveId);

        if (!leave.getEmployee().getId().equals(employee.getId())) {
            // Same message as "not found", so nobody can probe other people's leave ids
            throw new ResourceNotFoundException("Leave request not found: " + leaveId);
        }
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only PENDING leaves can be cancelled. This one is " + leave.getStatus());
        }
        leaveRepository.delete(leave);
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
            throw new BadRequestException("Only PENDING leaves can be approved. Current status: " + leave.getStatus());
        }

        // Re-check the quota at approval time, so HR can never approve past 24 paid days
        if (leave.getLeaveType() != LeaveType.UNPAID) {
            for (int year = leave.getFromDate().getYear(); year <= leave.getToDate().getYear(); year++) {
                long approved  = paidDaysInYear(leave.getEmployee().getId(), LeaveStatus.APPROVED, year);
                long requested = workingDaysInYear(leave.getFromDate(), leave.getToDate(), year);
                if (approved + requested > TOTAL_ANNUAL_QUOTA) {
                    throw new BadRequestException("Cannot approve: the employee has only "
                            + Math.max(0, TOTAL_ANNUAL_QUOTA - approved) + " paid leave day(s) left in " + year
                            + " but this request needs " + requested + ". Reject it or ask for Unpaid Leave.");
                }
            }
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setHrRemarks(hrRemarks);
        leave.setReviewedAt(LocalDateTime.now());

        markAttendanceOnLeave(leave);
        return leaveRepository.save(leave);
    }

    @Transactional
    public LeaveRequest rejectLeave(Long leaveId, String hrRemarks) {
        LeaveRequest leave = getLeaveById(leaveId);

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only PENDING leaves can be rejected. Current status: " + leave.getStatus());
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

    // After approval: every working day of the leave shows as ON_LEAVE in attendance.
    // A day the employee already checked in (they actually worked) is left as it is.
    private void markAttendanceOnLeave(LeaveRequest leave) {
        Employee employee = leave.getEmployee();
        for (LocalDate day = leave.getFromDate(); !day.isAfter(leave.getToDate()); day = day.plusDays(1)) {
            if (isWeekend(day)) continue;

            final LocalDate date = day;
            Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), date)
                    .orElse(Attendance.builder().employee(employee).date(date).build());
            if (attendance.getCheckIn() != null) continue;

            attendance.setStatus(Attendance.AttendanceStatus.ON_LEAVE);
            attendance.setRemarks("Approved leave (" + leave.getLeaveType() + ")");
            attendanceRepository.save(attendance);
        }
    }

    // Saturday and Sunday are not working days
    private boolean isWeekend(LocalDate day) {
        return day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    // Working days (Mon-Fri) from "from" to "to", both included
    private long workingDays(LocalDate from, LocalDate to) {
        long count = 0;
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            if (!isWeekend(day)) count++;
        }
        return count;
    }

    // Only the working days of the leave that fall inside the given year
    private long workingDaysInYear(LocalDate from, LocalDate to, int year) {
        LocalDate start = from.isBefore(LocalDate.of(year, 1, 1)) ? LocalDate.of(year, 1, 1) : from;
        LocalDate end   = to.isAfter(LocalDate.of(year, 12, 31)) ? LocalDate.of(year, 12, 31) : to;
        return start.isAfter(end) ? 0 : workingDays(start, end);
    }

    // Total PAID working days (UNPAID not counted) for one employee, status and year.
    // An employee has only a few leaves a year, so summing in Java is cheap.
    private long paidDaysInYear(Long employeeId, LeaveStatus status, int year) {
        return leaveRepository.findByEmployeeIdAndStatusAndLeaveTypeNot(employeeId, status, LeaveType.UNPAID)
                .stream()
                .mapToLong(l -> workingDaysInYear(l.getFromDate(), l.getToDate(), year))
                .sum();
    }
}
