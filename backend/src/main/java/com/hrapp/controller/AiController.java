package com.hrapp.controller;

import com.hrapp.entity.Candidate;
import com.hrapp.service.InterviewQuestionService;
import com.hrapp.service.ResumeScreenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final ResumeScreenerService resumeScreenerService;
    private final InterviewQuestionService interviewQuestionService;

    /**
     * POST /api/ai/screen/{candidateId}
     * HR only — trigger AI resume screening for a candidate.
     *
     * What happens:
     *  1. Loads candidate + their job's description and requiredSkills
     *  2. Sends to Groq API with a structured prompt
     *  3. Parses JSON response
     *  4. Saves aiScore, matchingSkills, missingSkills, aiSummary to DB
     *  5. Returns updated candidate object
     *
     * Prerequisites: candidate must have resumeText (extracted during PDF upload)
     */
    @PostMapping("/screen/{candidateId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Candidate> screenResume(@PathVariable Long candidateId) {
        return ResponseEntity.ok(resumeScreenerService.screenResume(candidateId));
    }

    /**
     * POST /api/ai/screen/job/{jobPostingId}
     * HR only — batch screen ALL unscreened candidates for a specific job.
     * Calls Groq for each candidate that has no aiScore yet.
     * Returns a summary count.
     */
    @PostMapping("/screen/job/{jobPostingId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<String> screenAllForJob(
            @PathVariable Long jobPostingId,
            @RequestParam(defaultValue = "false") boolean rescreenAll) {

        // Delegate to screener — it will handle internally
        // For simplicity this endpoint is a placeholder;
        // HR can loop on screen/{candidateId} or implement batch here
        return ResponseEntity.ok(
            "Use POST /api/ai/screen/{candidateId} to screen individual candidates. " +
            "Batch endpoint can be wired to ResumeScreenerService if needed.");
    }

    /**
     * POST /api/ai/questions/{candidateId}
     * HR only — generate 5 personalised interview questions for a candidate.
     *
     * What happens:
     *  1. Loads candidate's resume text and job title
     *  2. Sends to Groq API with interview question prompt
     *  3. Parses JSON array of 5 questions
     *  4. Saves as JSON string in interviewQuestions column
     *  5. Returns updated candidate object
     *
     * Prerequisites: candidate must have resumeText
     */
    @PostMapping("/questions/{candidateId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Candidate> generateInterviewQuestions(
            @PathVariable Long candidateId) {
        return ResponseEntity.ok(
                interviewQuestionService.generateQuestions(candidateId));
    }
}
