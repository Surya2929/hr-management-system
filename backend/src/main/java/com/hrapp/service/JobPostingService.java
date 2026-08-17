package com.hrapp.service;

import com.hrapp.entity.JobPosting;
import com.hrapp.entity.JobPosting.JobStatus;
import com.hrapp.entity.User;
import com.hrapp.repository.JobPostingRepository;
import com.hrapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;

    // ── Public: list all OPEN jobs ────────────────────────
    public List<JobPosting> getOpenJobs() {
        return jobPostingRepository.findByStatusOrderByCreatedAtDesc(JobStatus.OPEN);
    }

    // ── HR: list all jobs (OPEN + CLOSED) ────────────────
    public List<JobPosting> getAllJobs() {
        return jobPostingRepository.findAllByOrderByCreatedAtDesc();
    }

    // ── Get by ID (used by candidate apply form to pre-fill job title) ──
    public JobPosting getJobById(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job posting not found: " + id));
    }

    // ── HR: create a new job posting ─────────────────────
    @Transactional
    public JobPosting createJob(Map<String, Object> body, Long hrUserId) {
        User hrUser = userRepository.findById(hrUserId)
                .orElseThrow(() -> new RuntimeException("HR user not found"));

        JobPosting job = JobPosting.builder()
                .title(getRequired(body, "title"))
                .description(body.containsKey("description")
                        ? body.get("description").toString() : null)
                .requiredSkills(body.containsKey("requiredSkills")
                        ? body.get("requiredSkills").toString() : null)
                .location(body.containsKey("location")
                        ? body.get("location").toString() : null)
                .employmentType(body.containsKey("employmentType")
                        ? JobPosting.EmploymentType.valueOf(
                                body.get("employmentType").toString().toUpperCase())
                        : JobPosting.EmploymentType.FULL_TIME)
                .status(JobStatus.OPEN)
                .createdBy(hrUser)
                .build();

        return jobPostingRepository.save(job);
    }

    // ── HR: update a job posting ──────────────────────────
    @Transactional
    public JobPosting updateJob(Long id, Map<String, Object> body) {
        JobPosting job = getJobById(id);

        if (body.containsKey("title"))
            job.setTitle(body.get("title").toString());

        if (body.containsKey("description"))
            job.setDescription(body.get("description").toString());

        if (body.containsKey("requiredSkills"))
            job.setRequiredSkills(body.get("requiredSkills").toString());

        if (body.containsKey("location"))
            job.setLocation(body.get("location").toString());

        if (body.containsKey("employmentType"))
            job.setEmploymentType(JobPosting.EmploymentType.valueOf(
                    body.get("employmentType").toString().toUpperCase()));

        if (body.containsKey("status"))
            job.setStatus(JobStatus.valueOf(
                    body.get("status").toString().toUpperCase()));

        return jobPostingRepository.save(job);
    }

    // ── HR: close a job posting ───────────────────────────
    @Transactional
    public JobPosting closeJob(Long id) {
        JobPosting job = getJobById(id);
        job.setStatus(JobStatus.CLOSED);
        return jobPostingRepository.save(job);
    }

    // ── HR: reopen a closed job posting ──────────────────
    @Transactional
    public JobPosting reopenJob(Long id) {
        JobPosting job = getJobById(id);
        job.setStatus(JobStatus.OPEN);
        return jobPostingRepository.save(job);
    }

    // ── HR: delete a job posting ──────────────────────────
    @Transactional
    public void deleteJob(Long id) {
        getJobById(id); // throws if not found
        jobPostingRepository.deleteById(id);
    }

    // ── Helper ────────────────────────────────────────────
    private String getRequired(Map<String, Object> body, String key) {
        if (!body.containsKey(key) || body.get(key) == null) {
            throw new RuntimeException("Missing required field: " + key);
        }
        return body.get(key).toString();
    }
}
