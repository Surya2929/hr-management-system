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
                .orElseThrow(() -> new RuntimeException("Job posting not found: " + jobPostingId));

        String savedFilePath = saveResumeToDisk(resumeFile, email);

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
                .resumeFilePath(savedFilePath)
                .resumeText(resumeText)
                .status(CandidateStatus.APPLIED)
                .referredByEmployee(referredByEmail)
                .build();

        return candidateRepository.save(candidate);
    }

    public List<Candidate> getCandidatesByJob(Long jobPostingId) {
        jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new RuntimeException("Job posting not found: " + jobPostingId));
        return candidateRepository.findByJobPostingIdOrderByAiScoreDescAppliedAtAsc(jobPostingId);
    }

    public List<Candidate> getAllCandidatesRanked() {
        return candidateRepository.findAllByOrderByAiScoreDescAppliedAtAsc();
    }

    public List<Candidate> getCandidatesByJobAndStatus(Long jobPostingId, CandidateStatus status) {
        return candidateRepository.findByJobPostingIdAndStatusOrderByAiScoreDesc(jobPostingId, status);
    }

    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found: " + id));
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
        candidateRepository.deleteById(id);
    }

    public Candidate save(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    private String saveResumeToDisk(MultipartFile file, String email) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Resume file is required");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("Only PDF files are accepted for resumes");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String uniqueFilename = UUID.randomUUID() + "_" +
                email.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";

        Path targetPath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toString();
    }
}