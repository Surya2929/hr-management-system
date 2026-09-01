package com.hrapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobDescriptionService {

    private final GroqApiService groqApiService;

    public String generateDescription(String title, String skills) {
        if (title == null || title.isBlank()) {
            throw new RuntimeException("Job title is required to generate a description.");
        }

        String systemPrompt = """
                You are an expert HR copywriter who writes clear, professional job descriptions.
                Write in plain text only — no markdown, no headers with '#', no bullet symbols like '*' or '-'.
                Keep it to 3-4 short paragraphs: a role overview, key responsibilities, and requirements.
                Do not invent a company name; refer to "our company" if needed.
                """;

        String userPrompt = String.format("""
                Write a professional job description for the following role.

                JOB TITLE: %s

                REQUIRED SKILLS: %s

                Write 3-4 paragraphs covering: a brief role overview, key responsibilities,
                and requirements based on the skills listed. Keep the tone professional and concise.
                """,
                title,
                (skills != null && !skills.isBlank()) ? skills : "Not specified"
        );

        return groqApiService.chat(systemPrompt, userPrompt).trim();
    }
}
