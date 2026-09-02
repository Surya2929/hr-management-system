package com.hrapp.controller;

import com.hrapp.entity.Candidate;
import com.hrapp.entity.Candidate.CandidateStatus;
import com.hrapp.entity.User;
import com.hrapp.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    /**
     * POST /api/candidates/add
     * HR only — upload a candidate's resume and details directly from HR portal
     */
    @PostMapping(value = "/add", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Candidate> addCandidate(
            @RequestParam("jobPostingId") Long jobPostingId,
            @RequestParam("fullName")     String fullName,
            @RequestParam("email")        String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam("resume")       MultipartFile resume) throws IOException {

        Candidate candidate = candidateService.applyForJob(
                jobPostingId, fullName, email, phone, resume);
        return ResponseEntity.status(HttpStatus.CREATED).body(candidate);
    }

    /**
     * POST /api/candidates/refer
     * EMPLOYEE only — refer someone for an open position from the Employee portal
     */
    @PostMapping(value = "/refer", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Candidate> referCandidate(
            @RequestParam("jobPostingId") Long jobPostingId,
            @RequestParam("fullName")     String fullName,
            @RequestParam("email")        String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam("resume")       MultipartFile resume,
            @AuthenticationPrincipal User currentUser) throws IOException {

        Candidate candidate = candidateService.referCandidate(
                jobPostingId, fullName, email, phone, resume, currentUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(candidate);
    }

    /**
     * GET /api/candidates/job/{jobPostingId}
     * HR only — all candidates for a specific job sorted by AI score
     */
    @GetMapping("/job/{jobPostingId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<Candidate>> getCandidatesByJob(
            @PathVariable Long jobPostingId,
            @RequestParam(required = false) String status) {

        List<Candidate> candidates;
        if (status != null) {
            candidates = candidateService.getCandidatesByJobAndStatus(
                    jobPostingId, CandidateStatus.valueOf(status.toUpperCase()));
        } else {
            candidates = candidateService.getCandidatesByJob(jobPostingId);
        }
        return ResponseEntity.ok(candidates);
    }

    /**
     * GET /api/candidates/ranked
     * HR only — all candidates globally sorted by AI score
     */
    @GetMapping("/ranked")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<Candidate>> getAllCandidatesRanked() {
        return ResponseEntity.ok(candidateService.getAllCandidatesRanked());
    }

    /**
     * GET /api/candidates/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Candidate> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    /**
     * PUT /api/candidates/{id}/status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Candidate> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null) {
            throw new RuntimeException("Missing required field: status");
        }
        return ResponseEntity.ok(candidateService.updateStatus(id, status));
    }

    /**
     * DELETE /api/candidates/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return ResponseEntity.ok("Candidate record deleted successfully");
    }
}