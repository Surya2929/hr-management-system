package com.hrapp.repository;

import com.hrapp.entity.CandidateResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CandidateResumeRepository extends JpaRepository<CandidateResume,Long> {
    Optional<CandidateResume> findByCandidateId(Long candidateId);

    // Delete the PDF of one candidate (before deleting the candidate itself)
    @Modifying
    @Query("DELETE FROM CandidateResume r WHERE r.candidate.id = :candidateId")
    void deleteByCandidateId(@Param("candidateId") Long candidateId);

    // Delete the PDFs of every candidate of a job (before deleting the job)
    @Modifying
    @Query("DELETE FROM CandidateResume r WHERE r.candidate.id IN " +
           "(SELECT c.id FROM Candidate c WHERE c.jobPosting.id = :jobId)")
    void deleteByJobPostingId(@Param("jobId") Long jobId);
}
