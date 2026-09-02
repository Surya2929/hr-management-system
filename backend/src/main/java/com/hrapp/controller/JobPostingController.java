package com.hrapp.controller;

import com.hrapp.entity.JobPosting;
import com.hrapp.entity.User;
import com.hrapp.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<JobPosting>> getAllJobs() {
        return ResponseEntity.ok(jobPostingService.getAllJobs());
    }

    /**
     * GET /api/jobs/open
     * HR + EMPLOYEE — open positions only, so employees can view and refer candidates
     */
    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('HR','EMPLOYEE')")
    public ResponseEntity<List<JobPosting>> getOpenJobs() {
        return ResponseEntity.ok(jobPostingService.getOpenJobs());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR','EMPLOYEE')")
    public ResponseEntity<JobPosting> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.getJobById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobPosting> createJob(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(jobPostingService.createJob(body, currentUser.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobPosting> updateJob(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(jobPostingService.updateJob(id, body));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobPosting> closeJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.closeJob(id));
    }

    @PutMapping("/{id}/reopen")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobPosting> reopenJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.reopenJob(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        jobPostingService.deleteJob(id);
        return ResponseEntity.ok("Job posting deleted successfully");
    }
}