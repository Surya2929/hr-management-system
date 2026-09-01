package com.hrapp.repository;
import com.hrapp.entity.Candidate;
import com.hrapp.entity.Candidate.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CandidateRepository extends JpaRepository<Candidate,Long> {
    List<Candidate> findByJobPostingIdOrderByAiScoreDescAppliedAtAsc(Long jobPostingId);
    List<Candidate> findAllByOrderByAiScoreDescAppliedAtAsc();
    List<Candidate> findByJobPostingIdAndStatusOrderByAiScoreDesc(Long jobPostingId, CandidateStatus status);
    long countByJobPostingId(Long jobPostingId);
}
