package com.hrapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hrapp.entity.Candidate;
import com.hrapp.service.InterviewQuestionService;
import com.hrapp.service.JobDescriptionService;
import com.hrapp.service.ResumeScreenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI", description = "Groq-powered resume screening, questions and job descriptions")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final ResumeScreenerService resumeScreenerService;
    private final InterviewQuestionService interviewQuestionService;
    private final JobDescriptionService jobDescriptionService;

    @Operation(summary = "AI-score a candidate's resume against the job (HR)")
    @PostMapping("/screen/{candidateId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Candidate> screenResume(@PathVariable Long candidateId) {
        return ResponseEntity.ok(resumeScreenerService.screenResume(candidateId));
    }

    @Operation(summary = "Generate 5 interview questions for a candidate (HR)")
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
    @Operation(summary = "Generate a job description from title and skills (HR)")
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