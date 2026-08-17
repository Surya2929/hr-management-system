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

    // ════════════════════════════════════════════════════
    //  PUBLIC ENDPOINTS (no token required)
    // ════════════════════════════════════════════════════

    /**
     * GET /api/jobs/public
     * No auth — shows only OPEN jobs for the public job listing page
     * Candidates use this to browse and pick a job before applying
     */
    @GetMapping("/public")
    public ResponseEntity<List<JobPosting>> getOpenJobs() {
        return ResponseEntity.ok(jobPostingService.getOpenJobs());
    }

    /**
     * GET /api/jobs/public/{id}
     * No auth — get a single job's details (for the application form pre-fill)
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<JobPosting> getPublicJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.getJobById(id));
    }

    // ════════════════════════════════════════════════════
    //  AUTHENTICATED ENDPOINTS
    // ════════════════════════════════════════════════════

    /**
     * GET /api/jobs
     * HR only — all jobs (OPEN + CLOSED), newest first
     */
    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<JobPosting>> getAllJobs() {
        return ResponseEntity.ok(jobPostingService.getAllJobs());
    }

    /**
     * GET /api/jobs/{id}
     * HR only — get a single job by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobPosting> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.getJobById(id));
    }

    /**
     * POST /api/jobs
     * HR only — create a new job posting
     *
     * Body:
     * {
     *   "title":          "Backend Developer",        // required
     *   "description":    "We are looking for...",
     *   "requiredSkills": "Java, Spring Boot, MySQL",
     *   "location":       "Bangalore / Remote",
     *   "employmentType": "FULL_TIME"                 // FULL_TIME | PART_TIME | CONTRACT | INTERNSHIP
     * }
     */
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobPosting> createJob(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(jobPostingService.createJob(body, currentUser.getId()));
    }

    /**
     * PUT /api/jobs/{id}
     * HR only — update any fields on an existing posting
     *
     * Body (send only fields you want to change):
     * {
     *   "title":       "Senior Backend Developer",
     *   "status":      "CLOSED",
     *   "location":    "Remote Only"
     * }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobPosting> updateJob(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(jobPostingService.updateJob(id, body));
    }

    /**
     * PUT /api/jobs/{id}/close
     * HR only — quick shortcut to close a job posting
     */
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobPosting> closeJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.closeJob(id));
    }

    /**
     * PUT /api/jobs/{id}/reopen
     * HR only — quick shortcut to reopen a closed posting
     */
    @PutMapping("/{id}/reopen")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobPosting> reopenJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobPostingService.reopenJob(id));
    }

    /**
     * DELETE /api/jobs/{id}
     * HR only — delete a job posting
     * Note: cascades to candidates (via FK ON DELETE CASCADE in schema)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> deleteJob(@PathVariable Long id) {
        jobPostingService.deleteJob(id);
        return ResponseEntity.ok("Job posting deleted successfully");
    }
}
