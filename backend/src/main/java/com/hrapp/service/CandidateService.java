package com.hrapp.service;

import com.hrapp.entity.Candidate;
import com.hrapp.entity.Candidate.CandidateStatus;
import com.hrapp.entity.CandidateResume;
import com.hrapp.entity.JobPosting;
import com.hrapp.exception.ResourceNotFoundException;
import com.hrapp.repository.CandidateRepository;
import com.hrapp.repository.CandidateResumeRepository;
import com.hrapp.repository.JobPostingRepository;
import com.hrapp.util.PdfTextExtractor;
import com.hrapp.util.ResumeFileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateResumeRepository resumeRepository;

    @Transactional
    public Candidate applyForJob(Long jobPostingId,
                                  String fullName,
                                  String email,
                                  String phone,
                                  MultipartFile resumeFile) throws IOException {
        return saveCandidate(jobPostingId, fullName, email, phone, resumeFile, null);
    }

    /**
     * Used when an EMPLOYEE refers a candidate from the Open Positions page.
     * referredByEmail identifies which employee made the referral.
     */
    @Transactional
    public Candidate referCandidate(Long jobPostingId,
                                     String fullName,
                                     String email,
                                     String phone,
                                     MultipartFile resumeFile,
                                     String referredByEmail) throws IOException {
        return saveCandidate(jobPostingId, fullName, email, phone, resumeFile, referredByEmail);
    }

    private Candidate saveCandidate(Long jobPostingId,
                                     String fullName,
                                     String email,
                                     String phone,
                                     MultipartFile resumeFile,
                                     String referredByEmail) throws IOException {

        JobPosting job = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found: " + jobPostingId));

        // PDF only, max 5 MB, not empty (throws a clear 400 error otherwise)
        ResumeFileValidator.validate(resumeFile);

        String resumeText = "";
        try {
            resumeText = PdfTextExtractor.extractText(resumeFile);
        } catch (Exception e) {
            System.err.println("Warning: PDF text extraction failed for " + email + " — " + e.getMessage());
        }

        Candidate candidate = Candidate.builder()
                .jobPosting(job)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .resumeText(resumeText)
                .status(CandidateStatus.APPLIED)
                .referredByEmployee(referredByEmail)
                .build();

        candidate = candidateRepository.save(candidate);

        // Store the PDF itself in the database (Render's disk is wiped on restart).
        // Same transaction: if this fails, the candidate is not saved either.
        resumeRepository.save(CandidateResume.builder()
                .candidate(candidate)
                .contentType("application/pdf")
                .sizeBytes(resumeFile.getSize())
                .data(resumeFile.getBytes())
                .build());

        return candidate;
    }

    // The stored PDF, for HR's "View resume" button
    @Transactional(readOnly = true)
    public CandidateResume getResume(Long candidateId) {
        getCandidateById(candidateId);
        return resumeRepository.findByCandidateId(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume file is not available for this candidate (it was uploaded before resumes were stored in the database)"));
    }

    public List<Candidate> getCandidatesByJob(Long jobPostingId) {
        jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found: " + jobPostingId));
        return candidateRepository.findByJobPostingIdOrderByAiScoreDescAppliedAtAsc(jobPostingId);
    }

    // Ranked list (best AI score first) with optional search / status / job filters, one page at a time
    public Page<Candidate> searchCandidates(String search, CandidateStatus status, Long jobId, Pageable pageable) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return candidateRepository.search(term, status, jobId, pageable);
    }

    public List<Candidate> getCandidatesByJobAndStatus(Long jobPostingId, CandidateStatus status) {
        return candidateRepository.findByJobPostingIdAndStatusOrderByAiScoreDesc(jobPostingId, status);
    }

    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + id));
    }

    @Transactional
    public Candidate updateStatus(Long id, String status) {
        Candidate candidate = getCandidateById(id);
        candidate.setStatus(CandidateStatus.valueOf(status.toUpperCase()));
        return candidateRepository.save(candidate);
    }

    @Transactional
    public void deleteCandidate(Long id) {
        getCandidateById(id);
        resumeRepository.deleteByCandidateId(id); // PDF first (foreign key)
        candidateRepository.deleteById(id);
    }

    public Candidate save(Candidate candidate) {
        return candidateRepository.save(candidate);
    }
}
