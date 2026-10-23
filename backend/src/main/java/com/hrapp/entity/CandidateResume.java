package com.hrapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * The resume PDF itself, stored in the database.
 * Render's disk is wiped on every restart, so files saved there were lost.
 * Kept in its own table so loading a candidate list never loads megabytes of PDF data.
 */
@Entity @Table(name="candidate_resumes")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CandidateResume {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="candidate_id", nullable=false, unique=true)
    private Candidate candidate;

    @Column(name="content_type", nullable=false, length=50)
    private String contentType;

    @Column(name="size_bytes", nullable=false)
    private Long sizeBytes;

    @Lob
    @Column(nullable=false, columnDefinition="MEDIUMBLOB") // up to 16 MB; uploads are capped at 5 MB
    private byte[] data;

    @Column(name="uploaded_at", updatable=false)
    private LocalDateTime uploadedAt;

    @PrePersist protected void onCreate() { uploadedAt = LocalDateTime.now(); }
}
