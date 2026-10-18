package com.hrapp.service;

import com.hrapp.dto.DashboardStatsResponse;
import com.hrapp.entity.Attendance.AttendanceStatus;
import com.hrapp.entity.Candidate.CandidateStatus;
import com.hrapp.entity.JobPosting.JobStatus;
import com.hrapp.entity.LeaveRequest.LeaveStatus;
import com.hrapp.repository.AttendanceRepository;
import com.hrapp.repository.CandidateRepository;
import com.hrapp.repository.EmployeeRepository;
import com.hrapp.repository.JobPostingRepository;
import com.hrapp.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final CandidateRepository candidateRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final JobPostingRepository jobPostingRepository;

    public DashboardStatsResponse getStats() {
        // Start every status at 0 so the charts always show all categories
        Map<String, Long> candidatesByStatus = new LinkedHashMap<>();
        for (CandidateStatus s : CandidateStatus.values()) candidatesByStatus.put(s.name(), 0L);
        putAll(candidatesByStatus, candidateRepository.countByStatus());

        Map<String, Long> attendanceToday = new LinkedHashMap<>();
        for (AttendanceStatus s : AttendanceStatus.values()) attendanceToday.put(s.name(), 0L);
        putAll(attendanceToday, attendanceRepository.countByStatusOnDate(LocalDate.now()));

        Map<String, Long> employeesByDepartment = new LinkedHashMap<>();
        putAll(employeesByDepartment, employeeRepository.countByDepartment());

        long totalCandidates = candidatesByStatus.values().stream().mapToLong(Long::longValue).sum();

        return DashboardStatsResponse.builder()
                .totalEmployees(employeeRepository.count())
                .openJobs(jobPostingRepository.countByStatus(JobStatus.OPEN))
                .totalCandidates(totalCandidates)
                .pendingLeaves(leaveRequestRepository.countByStatus(LeaveStatus.PENDING))
                .employeesByDepartment(employeesByDepartment)
                .candidatesByStatus(candidatesByStatus)
                .attendanceToday(attendanceToday)
                .build();
    }

    // Each GROUP BY row is [label, count] -> put it into the map
    private void putAll(Map<String, Long> target, List<Object[]> rows) {
        for (Object[] row : rows) {
            target.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
    }
}
