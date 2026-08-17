package com.hrapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * Maps the JSON object returned by Groq for the interview question generator.
 *
 * Expected Groq response format:
 * {
 *   "questions": [
 *     "Explain your experience with Spring Boot dependency injection.",
 *     "How have you handled database transactions in MySQL?",
 *     ...
 *   ]
 * }
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewQuestionsResponse {
    private List<String> questions;
}
