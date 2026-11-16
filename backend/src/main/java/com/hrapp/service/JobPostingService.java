package com.hrapp.service;

import com.hrapp.util.InputCheck;
import com.hrapp.exception.ResourceNotFoundException;
import com.hrapp.entity.JobPosting;
import com.hrapp.entity.JobPosting.JobStatus;
import com.hrapp.entity.User;
import com.hrapp.repository.CandidateRepository;
import com.hrapp.repository.CandidateResumeRepository;
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
    private final CandidateRepository candidateRepository;
    private final CandidateResumeRepository resumeRepository;

    public List<JobPosting> getOpenJobs() {
        return jobPostingRepository.findByStatusOrderByCreatedAtDesc(JobStatus.OPEN);
    }

    public List<JobPosting> getAllJobs() {
        return jobPostingRepository.findAllByOrderByCreatedAtDesc();
    }

    public JobPosting getJobById(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found: " + id));
    }

    @Transactional
    public JobPosting createJob(Map<String, Object> body, Long hrUserId) {
        User hrUser = userRepository.findById(hrUserId)
                .orElseThrow(() -> new ResourceNotFoundException("HR user not found"));

        // Title is required (a missing title used to crash with a NullPointerException);
        // lengths match the DB columns
        JobPosting job = JobPosting.builder()
                .title(InputCheck.requiredText(body.get("title"), "Job title", 200))
                .description(InputCheck.optionalText(body.get("description"), "Description", 10_000))
                .requiredSkills(InputCheck.optionalText(body.get("requiredSkills"), "Required skills", 2_000))
                .location(InputCheck.optionalText(body.get("location"), "Location", 100))
                .salary(InputCheck.optionalText(body.get("salary"), "Salary", 100))
                .employmentType(body.containsKey("employmentType") && body.get("employmentType") != null
                        ? JobPosting.EmploymentType.valueOf(body.get("employmentType").toString().toUpperCase())
                        : JobPosting.EmploymentType.FULL_TIME)
                .status(JobStatus.OPEN)
                .createdBy(hrUser)
                .build();

        return jobPostingRepository.save(job);
    }

    @Transactional
    public JobPosting updateJob(Long id, Map<String, Object> body) {
        JobPosting job = getJobById(id);

        if (body.containsKey("title"))
            job.setTitle(InputCheck.requiredText(body.get("title"), "Job title", 200));

        if (body.containsKey("description"))
            job.setDescription(InputCheck.optionalText(body.get("description"), "Description", 10_000));

        if (body.containsKey("requiredSkills"))
            job.setRequiredSkills(InputCheck.optionalText(body.get("requiredSkills"), "Required skills", 2_000));

        if (body.containsKey("location"))
            job.setLocation(InputCheck.optionalText(body.get("location"), "Location", 100));

        if (body.containsKey("salary"))
            job.setSalary(InputCheck.optionalText(body.get("salary"), "Salary", 100));

        if (body.containsKey("employmentType") && body.get("employmentType") != null)
            job.setEmploymentType(JobPosting.EmploymentType.valueOf(body.get("employmentType").toString().toUpperCase()));

        if (body.containsKey("status") && body.get("status") != null)
            job.setStatus(JobStatus.valueOf(body.get("status").toString().toUpperCase()));

        return jobPostingRepository.save(job);
    }

    @Transactional
    public JobPosting closeJob(Long id) {
        JobPosting job = getJobById(id);
        job.setStatus(JobStatus.CLOSED);
        return jobPostingRepository.save(job);
    }

    @Transactional
    public JobPosting reopenJob(Long id) {
        JobPosting job = getJobById(id);
        job.setStatus(JobStatus.OPEN);
        return jobPostingRepository.save(job);
    }

    @Transactional
    public void deleteJob(Long id) {
        getJobById(id);
        // Children first (foreign keys): resume PDFs -> candidates -> the job itself
        resumeRepository.deleteByJobPostingId(id);
        candidateRepository.deleteByJobPostingId(id);
        jobPostingRepository.deleteById(id);
    }
}