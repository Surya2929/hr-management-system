package com.hrapp.controller;

import com.hrapp.entity.Candidate;
import com.hrapp.service.InterviewQuestionService;
import com.hrapp.service.JobDescriptionService;
import com.hrapp.service.ResumeScreenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final ResumeScreenerService resumeScreenerService;
    private final InterviewQuestionService interviewQuestionService;
    private final JobDescriptionService jobDescriptionService;

    @PostMapping("/screen/{candidateId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Candidate> screenResume(@PathVariable Long candidateId) {
        return ResponseEntity.ok(resumeScreenerService.screenResume(candidateId));
    }

    @PostMapping("/questions/{candidateId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Candidate> generateInterviewQuestions(@PathVariable Long candidateId) {
        return ResponseEntity.ok(interviewQuestionService.generateQuestions(candidateId));
    }

    /**
     * POST /api/ai/generate-description
     * HR only — generates a job description from a title + skills list
     * Body: { "title": "Java Developer", "skills": "Java, Spring Boot, MySQL" }
     */
    @PostMapping("/generate-description")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Map<String, String>> generateJobDescription(@RequestBody Map<String, String> body) {
        String description = jobDescriptionService.generateDescription(
                body.get("title"),
                body.get("skills")
        );
        return ResponseEntity.ok(Map.of("description", description));
    }
}