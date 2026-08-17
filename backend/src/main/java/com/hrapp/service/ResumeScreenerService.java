package com.hrapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrapp.dto.ResumeScoreResponse;
import com.hrapp.entity.Candidate;
import com.hrapp.util.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResumeScreenerService {

    private final GroqApiService groqApiService;
    private final CandidateService candidateService;
    private final ObjectMapper objectMapper;

    private static final int MAX_RESUME_CHARS = 6000;  // keep Groq prompt manageable

    /**
     * Screen a candidate's resume against their applied job description.
     * Calls Groq API, parses JSON response, updates Candidate entity in DB.
     *
     * @param candidateId  ID of the candidate to screen
     * @return             updated Candidate with AI score fields populated
     */
    @Transactional
    public Candidate screenResume(Long candidateId) {
        Candidate candidate = candidateService.getCandidateById(candidateId);

        // Guard: need resume text extracted during upload
        if (candidate.getResumeText() == null || candidate.getResumeText().isBlank()) {
            throw new RuntimeException(
                    "Resume text is empty for candidate #" + candidateId +
                    ". PDF extraction may have failed during upload.");
        }

        String jobTitle       = candidate.getJobPosting().getTitle();
        String jobDescription = candidate.getJobPosting().getDescription();
        String requiredSkills = candidate.getJobPosting().getRequiredSkills();

        String resumeText = PdfTextExtractor.truncate(
                candidate.getResumeText(), MAX_RESUME_CHARS);

        // ── Build prompts ──────────────────────────────────────
        String systemPrompt = """
                You are an expert technical recruiter and resume screener.
                Your task is to evaluate a candidate's resume against a job description.
                You MUST respond ONLY with a valid JSON object — no extra text, 
                no markdown, no explanation outside the JSON.
                """;

        String userPrompt = String.format("""
                Evaluate the following resume against the job description below.

                JOB TITLE: %s

                JOB DESCRIPTION:
                %s

                REQUIRED SKILLS:
                %s

                CANDIDATE RESUME:
                %s

                Respond ONLY with this JSON structure (no markdown, no extra text):
                {
                  "score": <integer 0-100 representing how well this candidate matches>,
                  "matchingSkills": [<list of skills from the resume that match the job>],
                  "missingSkills":  [<list of required skills not found in the resume>],
                  "summary": "<2-3 sentence explanation of the score>"
                }
                """,
                jobTitle,
                jobDescription != null ? jobDescription : "Not specified",
                requiredSkills != null ? requiredSkills : "Not specified",
                resumeText
        );

        // ── Call Groq API ──────────────────────────────────────
        String rawResponse = groqApiService.chat(systemPrompt, userPrompt);

        // ── Parse JSON response ────────────────────────────────
        ResumeScoreResponse parsed = parseJsonSafely(rawResponse, ResumeScoreResponse.class);

        // ── Persist AI results back to Candidate ───────────────
        candidate.setAiScore(parsed.getScore());
        candidate.setMatchingSkills(toJsonString(parsed.getMatchingSkills()));
        candidate.setMissingSkills(toJsonString(parsed.getMissingSkills()));
        candidate.setAiSummary(parsed.getSummary());

        return candidateService.save(candidate);
    }

    // ── Helpers ────────────────────────────────────────────────

    /**
     * Try to parse Groq's response as JSON.
     * Groq may sometimes wrap response in ```json ... ``` — strip that first.
     */
    private <T> T parseJsonSafely(String raw, Class<T> type) {
        // Strip markdown code fences if model adds them
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```[a-zA-Z]*\\n?", "")
                             .replaceAll("```$", "")
                             .trim();
        }
        try {
            return objectMapper.readValue(cleaned, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to parse Groq JSON response: " + e.getMessage() +
                    "\nRaw response was: " + raw);
        }
    }

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
