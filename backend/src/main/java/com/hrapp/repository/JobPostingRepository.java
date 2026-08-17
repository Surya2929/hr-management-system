package com.hrapp.repository;

import com.hrapp.entity.JobPosting;
import com.hrapp.entity.JobPosting.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // Public listing — only open jobs
    List<JobPosting> findByStatusOrderByCreatedAtDesc(JobStatus status);

    // HR — all postings regardless of status, newest first
    List<JobPosting> findAllByOrderByCreatedAtDesc();
}
