package com.hrapp.repository;

import com.hrapp.entity.Candidate;
import com.hrapp.entity.Candidate.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    // HR: all candidates for a job, sorted by AI score descending
    // Unscreened candidates (null score) appear at the bottom
    List<Candidate> findByJobPostingIdOrderByAiScoreDescAppliedAtAsc(Long jobPostingId);

    // HR: all candidates across all jobs, ranked by AI score
    List<Candidate> findAllByOrderByAiScoreDescAppliedAtAsc();

    // HR: filter candidates by status
    List<Candidate> findByJobPostingIdAndStatusOrderByAiScoreDesc(
            Long jobPostingId, CandidateStatus status);

    // Count of candidates per job (for dashboard stats)
    long countByJobPostingId(Long jobPostingId);
}
