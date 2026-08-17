package com.hrapp.service;

import com.hrapp.entity.Candidate;
import com.hrapp.entity.Candidate.CandidateStatus;
import com.hrapp.entity.JobPosting;
import com.hrapp.repository.CandidateRepository;
import com.hrapp.repository.JobPostingRepository;
import com.hrapp.util.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final JobPostingRepository jobPostingRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ── PUBLIC: Submit a job application with resume PDF ──
    @Transactional
    public Candidate applyForJob(Long jobPostingId,
                                  String fullName,
                                  String email,
                                  String phone,
                                  MultipartFile resumeFile) throws IOException {

        // 1. Validate job posting exists and is still OPEN
        JobPosting job = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new RuntimeException("Job posting not found: " + jobPostingId));

        if (job.getStatus() == JobPosting.JobStatus.CLOSED) {
            throw new RuntimeException("This job posting is no longer accepting applications");
        }

        // 2. Save the uploaded PDF to disk
        String savedFilePath = saveResumeToDisk(resumeFile, email);

        // 3. Extract text from the PDF
        String resumeText = "";
        try {
            resumeText = PdfTextExtractor.extractText(resumeFile);
        } catch (IOException e) {
            // Don't block the application if PDF parsing fails — store empty text
            // HR can still review the uploaded file manually
            System.err.println("Warning: PDF text extraction failed for " + email
                    + " — " + e.getMessage());
        }

        // 4. Build and save candidate
        Candidate candidate = Candidate.builder()
                .jobPosting(job)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .resumeFilePath(savedFilePath)
                .resumeText(resumeText)
                .status(CandidateStatus.APPLIED)
                .build();

        return candidateRepository.save(candidate);
    }

    // ── HR: All candidates for a specific job (sorted by AI score) ──
    public List<Candidate> getCandidatesByJob(Long jobPostingId) {
        // Verify job exists
        jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new RuntimeException("Job posting not found: " + jobPostingId));
        return candidateRepository
                .findByJobPostingIdOrderByAiScoreDescAppliedAtAsc(jobPostingId);
    }

    // ── HR: All candidates across all jobs (global ranked list) ─────
    public List<Candidate> getAllCandidatesRanked() {
        return candidateRepository.findAllByOrderByAiScoreDescAppliedAtAsc();
    }

    // ── HR: Candidates for a job filtered by status ──────
    public List<Candidate> getCandidatesByJobAndStatus(Long jobPostingId,
                                                        CandidateStatus status) {
        return candidateRepository
                .findByJobPostingIdAndStatusOrderByAiScoreDesc(jobPostingId, status);
    }

    // ── HR: Get a single candidate by ID ─────────────────
    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found: " + id));
    }

    // ── HR: Update candidate status (shortlist / reject / hire) ──
    @Transactional
    public Candidate updateStatus(Long id, String status) {
        Candidate candidate = getCandidateById(id);
        candidate.setStatus(CandidateStatus.valueOf(status.toUpperCase()));
        return candidateRepository.save(candidate);
    }

    // ── HR: Delete a candidate application ───────────────
    @Transactional
    public void deleteCandidate(Long id) {
        getCandidateById(id); // throws if not found
        candidateRepository.deleteById(id);
    }

    // ── Internal: used by AI services to save back to DB ─
    public Candidate save(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    // ── Private: save resume PDF to upload directory ─────
    private String saveResumeToDisk(MultipartFile file, String email) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Resume file is required");
        }

        // Validate it's a PDF
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("Only PDF files are accepted for resumes");
        }

        // Create uploads directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // Generate a unique filename to prevent collisions
        String uniqueFilename = UUID.randomUUID() + "_" +
                email.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";

        Path targetPath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }
}
