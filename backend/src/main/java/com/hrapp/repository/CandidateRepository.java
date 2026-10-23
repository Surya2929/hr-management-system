package com.hrapp.repository;

import com.hrapp.entity.Candidate;
import com.hrapp.entity.Candidate.CandidateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate,Long> {
    List<Candidate> findByJobPostingIdOrderByAiScoreDescAppliedAtAsc(Long jobPostingId);
    List<Candidate> findAllByOrderByAiScoreDescAppliedAtAsc();
    List<Candidate> findByJobPostingIdAndStatusOrderByAiScoreDesc(Long jobPostingId, CandidateStatus status);
    long countByJobPostingId(Long jobPostingId);

    // Ranked search: best AI score first. A null parameter means "no filter".
    @Query("SELECT c FROM Candidate c WHERE " +
           "(:search IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:jobId IS NULL OR c.jobPosting.id = :jobId) " +
           "ORDER BY c.aiScore DESC, c.appliedAt ASC")
    Page<Candidate> search(@Param("search") String search,
                           @Param("status") CandidateStatus status,
                           @Param("jobId") Long jobId,
                           Pageable pageable);

    // Dashboard: candidates per status (hiring funnel)
    @Query("SELECT c.status, COUNT(c) FROM Candidate c GROUP BY c.status")
    List<Object[]> countByStatus();

    // Used when a job posting is deleted: removes all its applications in one query
    @Modifying
    @Query("DELETE FROM Candidate c WHERE c.jobPosting.id = :jobId")
    void deleteByJobPostingId(@Param("jobId") Long jobId);
}
