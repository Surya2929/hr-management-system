package com.hrapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="candidates")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Candidate {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"createdBy","hibernateLazyInitializer","handler"})
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="job_posting_id", nullable=false)
    private JobPosting jobPosting;

    @Column(name="full_name", nullable=false, length=200)
    private String fullName;

    @Column(nullable=false, length=150)
    private String email;

    @Column(length=20)
    private String phone;

    @Column(name="resume_file_path", length=500)
    private String resumeFilePath;

    @Column(name="resume_text", columnDefinition="MEDIUMTEXT")
    private String resumeText;

    // AI Resume Screener fields
    @Column(name="ai_score")
    private Integer aiScore;

    @Column(name="matching_skills", columnDefinition="TEXT")
    private String matchingSkills;    // JSON array string

    @Column(name="missing_skills", columnDefinition="TEXT")
    private String missingSkills;     // JSON array string

    @Column(name="ai_summary", columnDefinition="TEXT")
    private String aiSummary;

    // AI Interview Questions
    @Column(name="interview_questions", columnDefinition="TEXT")
    private String interviewQuestions; // JSON array string

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CandidateStatus status = CandidateStatus.APPLIED;

    @Column(name="applied_at", updatable=false)
    private LocalDateTime appliedAt;

    @PrePersist protected void onCreate() { appliedAt = LocalDateTime.now(); }

    public enum CandidateStatus { APPLIED, SHORTLISTED, REJECTED, HIRED }
}
