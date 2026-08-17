package com.hrapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Makes REST calls to the Groq API (OpenAI-compatible endpoint).
 * All AI features funnel through this single service.
 */
@Service
@RequiredArgsConstructor
public class GroqApiService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Send a prompt to Groq and return the raw response text.
     *
     * @param systemPrompt  context/instructions for the AI
     * @param userPrompt    the actual content (resume text, job description, etc.)
     * @return              raw string response from the model
     */
    public String chat(String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user",   "content", userPrompt)
            ),
            "temperature", 0.3,   // low temp → consistent, structured responses
            "max_tokens",  1500
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            // Parse: response.choices[0].message.content
            JsonNode root    = objectMapper.readTree(response.getBody());
            JsonNode content = root.path("choices").get(0).path("message").path("content");
            return content.asText();

        } catch (Exception e) {
            throw new RuntimeException("Groq API call failed: " + e.getMessage(), e);
        }
    }
}
