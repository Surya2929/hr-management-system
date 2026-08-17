package com.hrapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * Maps the JSON object returned by Groq for the resume screening prompt.
 *
 * Expected Groq response format:
 * {
 *   "score": 78,
 *   "matchingSkills": ["Java", "Spring Boot", "MySQL"],
 *   "missingSkills":  ["Docker", "Kubernetes"],
 *   "summary": "The candidate has strong Java experience..."
 * }
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeScoreResponse {
    private int score;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private String summary;
}
