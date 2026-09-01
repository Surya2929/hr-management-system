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

    public List<JobPosting> getOpenJobs() {
        return jobPostingRepository.findByStatusOrderByCreatedAtDesc(JobStatus.OPEN);
    }

    public List<JobPosting> getAllJobs() {
        return jobPostingRepository.findAllByOrderByCreatedAtDesc();
    }

    public JobPosting getJobById(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job posting not found: " + id));
    }

    @Transactional
    public JobPosting createJob(Map<String, Object> body, Long hrUserId) {
        User hrUser = userRepository.findById(hrUserId)
                .orElseThrow(() -> new RuntimeException("HR user not found"));

        JobPosting job = JobPosting.builder()
                .title(body.get("title").toString())
                .description(body.containsKey("description") && body.get("description") != null ? body.get("description").toString() : null)
                .requiredSkills(body.containsKey("requiredSkills") && body.get("requiredSkills") != null ? body.get("requiredSkills").toString() : null)
                .location(body.containsKey("location") && body.get("location") != null ? body.get("location").toString() : null)
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

        if (body.containsKey("title") && body.get("title") != null)
            job.setTitle(body.get("title").toString());

        if (body.containsKey("description"))
            job.setDescription(body.get("description") != null ? body.get("description").toString() : null);

        if (body.containsKey("requiredSkills"))
            job.setRequiredSkills(body.get("requiredSkills") != null ? body.get("requiredSkills").toString() : null);

        if (body.containsKey("location"))
            job.setLocation(body.get("location") != null ? body.get("location").toString() : null);

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
        jobPostingRepository.deleteById(id);
    }
}
