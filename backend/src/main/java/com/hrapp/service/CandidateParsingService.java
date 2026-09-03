package com.hrapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrapp.util.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CandidateParsingService {

    private final GroqApiService groqApiService;
    private final ObjectMapper objectMapper;

    /**
     * Reads a resume PDF and asks the AI to pull out the candidate's
     * full name, email, and phone number. Used to auto-fill the
     * "Add Candidate" form so HR doesn't have to type these manually.
     */
    public Map<String, String> extractContactDetails(MultipartFile resumeFile) throws IOException {
        String resumeText = PdfTextExtractor.extractText(resumeFile);
        String truncated   = PdfTextExtractor.truncate(resumeText, 6000);

        Map<String, String> result = new HashMap<>();
        result.put("fullName", "");
        result.put("email", "");
        result.put("phone", "");
        result.put("resumeText", resumeText);

        if (truncated.isBlank()) {
            return result; // empty/unreadable PDF — let HR fill manually
        }

        String systemPrompt = """
                You extract contact details from resumes.
                Respond with ONLY valid JSON, no extra text, no markdown fences.
                JSON shape: {"fullName": "...", "email": "...", "phone": "..."}
                If a field cannot be found, use an empty string for it.
                Do not guess or invent an email or phone number that is not in the text.
                """;

        String userPrompt = "Extract the candidate's full name, email, and phone number from this resume text:\n\n" + truncated;

        try {
            String response = groqApiService.chat(systemPrompt, userPrompt);
            String cleaned = response.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```json", "").replaceAll("```", "").trim();
            }
            JsonNode node = objectMapper.readTree(cleaned);

            result.put("fullName", node.path("fullName").asText(""));
            result.put("email", node.path("email").asText(""));
            result.put("phone", node.path("phone").asText(""));
        } catch (Exception e) {
            // AI parsing failed — return empty fields, HR fills manually, not a hard failure
            System.err.println("Resume contact extraction failed: " + e.getMessage());
        }

        return result;
    }
}