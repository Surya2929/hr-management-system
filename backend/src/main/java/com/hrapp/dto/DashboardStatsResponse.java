package com.hrapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** Everything the HR dashboard needs, in one response. Maps are { label: count }. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalEmployees;
    private long openJobs;
    private long totalCandidates;
    private long pendingLeaves;

    private Map<String, Long> employeesByDepartment; // e.g. { "Engineering": 5 }
    private Map<String, Long> candidatesByStatus;    // APPLIED, SHORTLISTED, REJECTED, HIRED
    private Map<String, Long> attendanceToday;       // PRESENT, ABSENT, HALF_DAY, ON_LEAVE
}
