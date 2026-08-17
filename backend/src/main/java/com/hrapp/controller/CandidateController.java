package com.hrapp.controller;

import com.hrapp.entity.Candidate;
import com.hrapp.entity.Candidate.CandidateStatus;
import com.hrapp.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    // ════════════════════════════════════════════════════
    //  PUBLIC ENDPOINT — no token required
    // ════════════════════════════════════════════════════

    /**
     * POST /api/candidates/apply
     * Public — candidates submit their application with resume PDF.
     * Uses multipart/form-data (NOT JSON) because of file upload.
     *
     * Form fields:
     *   jobPostingId  (Long)         — ID of the job being applied for
     *   fullName      (String)       — candidate's full name
     *   email         (String)       — candidate's email
     *   phone         (String)       — optional phone number
     *   resume        (MultipartFile) — PDF resume file
     *
     * Example curl:
     *   curl -X POST http://localhost:8080/api/candidates/apply \
     *     -F "jobPostingId=1" \
     *     -F "fullName=John Doe" \
     *     -F "email=john@example.com" \
     *     -F "phone=9876543210" \
     *     -F "resume=@/path/to/resume.pdf"
     */
    @PostMapping(value = "/apply", consumes = "multipart/form-data")
    public ResponseEntity<Candidate> applyForJob(
            @RequestParam("jobPostingId") Long jobPostingId,
            @RequestParam("fullName")     String fullName,
            @RequestParam("email")        String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam("resume")       MultipartFile resume) throws IOException {

        Candidate candidate = candidateService.applyForJob(
                jobPostingId, fullName, email, phone, resume);
        return ResponseEntity.status(HttpStatus.CREATED).body(candidate);
    }

    // ════════════════════════════════════════════════════
    //  HR ENDPOINTS
    // ════════════════════════════════════════════════════

    /**
     * GET /api/candidates/job/{jobPostingId}
     * HR only — all candidates for a specific job, sorted by AI score descending.
     * Unscreened candidates (aiScore = null) appear at the bottom.
     *
     * Optional query param: ?status=SHORTLISTED|REJECTED|HIRED|APPLIED
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
     * HR only — all candidates across all jobs, sorted by AI score globally.
     * Useful for the HR dashboard overview.
     */
    @GetMapping("/ranked")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<Candidate>> getAllCandidatesRanked() {
        return ResponseEntity.ok(candidateService.getAllCandidatesRanked());
    }

    /**
     * GET /api/candidates/{id}
     * HR only — single candidate detail view
     * (shows resumeText, AI score, matching/missing skills, interview questions)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Candidate> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    /**
     * PUT /api/candidates/{id}/status
     * HR only — update candidate status
     *
     * Body:
     * { "status": "SHORTLISTED" }   // APPLIED | SHORTLISTED | REJECTED | HIRED
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Candidate> updateStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        String status = body.get("status");
        if (status == null) {
            throw new RuntimeException("Missing required field: status");
        }
        return ResponseEntity.ok(candidateService.updateStatus(id, status));
    }

    /**
     * DELETE /api/candidates/{id}
     * HR only — remove a candidate application
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return ResponseEntity.ok("Candidate application deleted");
    }
}
