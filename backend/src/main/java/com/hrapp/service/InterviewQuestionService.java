package com.hrapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrapp.dto.InterviewQuestionsResponse;
import com.hrapp.entity.Candidate;
import com.hrapp.util.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewQuestionService {

    private final GroqApiService groqApiService;
    private final CandidateService candidateService;
    private final ObjectMapper objectMapper;

    private static final int MAX_RESUME_CHARS = 4000;

    /**
     * Generate 5 personalised interview questions for a candidate
     * based on their resume and the job role they applied for.
     *
     * @param candidateId  ID of the candidate
     * @return             updated Candidate with interviewQuestions populated
     */
    @Transactional
    public Candidate generateQuestions(Long candidateId) {
        Candidate candidate = candidateService.getCandidateById(candidateId);

        if (candidate.getResumeText() == null || candidate.getResumeText().isBlank()) {
            throw new RuntimeException(
                    "Resume text is empty for candidate #" + candidateId +
                    ". Cannot generate interview questions.");
        }

        String jobTitle    = candidate.getJobPosting().getTitle();
        String resumeText  = PdfTextExtractor.truncate(
                candidate.getResumeText(), MAX_RESUME_CHARS);

        // ── Build prompts ──────────────────────────────────────
        String systemPrompt = """
                You are an experienced technical interviewer.
                Your task is to generate personalised interview questions
                based on the candidate's resume and the role they are applying for.
                You MUST respond ONLY with a valid JSON object — no extra text,
                no markdown, no explanation outside the JSON.
                """;

        String userPrompt = String.format("""
                Generate exactly 5 personalised technical interview questions
                for a candidate applying for the role of: %s

                Base the questions on skills and experience found in their resume below.
                Make each question specific — reference technologies, projects, 
                or experience mentioned in the resume.

                CANDIDATE RESUME:
                %s

                Respond ONLY with this JSON structure (no markdown, no extra text):
                {
                  "questions": [
                    "<question 1>",
                    "<question 2>",
                    "<question 3>",
                    "<question 4>",
                    "<question 5>"
                  ]
                }
                """,
                jobTitle,
                resumeText
        );

        // ── Call Groq API ──────────────────────────────────────
        String rawResponse = groqApiService.chat(systemPrompt, userPrompt);

        // ── Parse JSON response ────────────────────────────────
        InterviewQuestionsResponse parsed = parseJsonSafely(rawResponse);

        // ── Persist questions to Candidate row ─────────────────
        try {
            String questionsJson = objectMapper.writeValueAsString(parsed.getQuestions());
            candidate.setInterviewQuestions(questionsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize interview questions");
        }

        return candidateService.save(candidate);
    }

    // ── Helpers ────────────────────────────────────────────────

    private InterviewQuestionsResponse parseJsonSafely(String raw) {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```[a-zA-Z]*\\n?", "")
                             .replaceAll("```$", "")
                             .trim();
        }
        try {
            return objectMapper.readValue(cleaned, InterviewQuestionsResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to parse Groq JSON response: " + e.getMessage() +
                    "\nRaw response was: " + raw);
        }
    }
}
