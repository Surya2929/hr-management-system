package com.hrapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which job this candidate applied for
    @JsonIgnoreProperties({"createdBy", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    // ── Basic Info (from public application form) ─────────
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "resume_file_path", length = 500)
    private String resumeFilePath;     // path to the stored PDF on disk

    @Column(name = "resume_text", columnDefinition = "MEDIUMTEXT")
    private String resumeText;         // raw text extracted by PDFBox

    // ── AI Resume Screener fields ─────────────────────────
    @Column(name = "ai_score")
    private Integer aiScore;           // 0-100 match score (null = not yet screened)

    @Column(name = "matching_skills", columnDefinition = "TEXT")
    private String matchingSkills;     // JSON string: ["Java","Spring Boot"]

    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills;      // JSON string: ["Kubernetes","Redis"]

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;          // short paragraph reasoning from Groq

    // ── AI Interview Questions field ──────────────────────
    @Column(name = "interview_questions", columnDefinition = "TEXT")
    private String interviewQuestions; // JSON string: ["Q1?","Q2?","Q3?","Q4?","Q5?"]

    // ── Application Status ────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CandidateStatus status = CandidateStatus.APPLIED;

    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    protected void onCreate() {
        this.appliedAt = LocalDateTime.now();
    }

    // ── Status enum ───────────────────────────────────────
    public enum CandidateStatus {
        APPLIED, SHORTLISTED, REJECTED, HIRED
    }
}
