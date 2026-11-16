package com.hrapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="job_postings")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JobPosting {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=200)
    private String title;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(name="required_skills", columnDefinition="TEXT")
    private String requiredSkills;

    @Column(length=100)
    private String location;

    @Column(length=100)
    private String salary;

    @Enumerated(EnumType.STRING)
    @Column(name="employment_type")
    @Builder.Default
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JobStatus status = JobStatus.OPEN;

    // Never sent in JSON: the job list is also shown to employees and would leak the HR email
    @JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="created_by")
    private User createdBy;

    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @PrePersist  protected void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate   protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public enum EmploymentType { FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP }
    public enum JobStatus      { OPEN, CLOSED }
}