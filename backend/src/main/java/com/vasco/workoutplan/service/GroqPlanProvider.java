package com.vasco.workoutplan.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.vasco.workoutplan.model.GeneratedPlan;
import com.vasco.workoutplan.model.Intake;

/**
 * Groq implementation of PlanGenerationProvider.
 *
 * Used as the default free-tier provider instead of Gemini, because Gemini's
 * free tier excludes EU/EEA/UK/Switzerland — Groq has no such restriction.
 * This class is the practical payoff of the provider abstraction: the request
 * and response shapes here are completely different from GeminiPlanProvider
 * (OpenAI-compatible chat completions vs Gemini's own format), but neither
 * PlanService nor PlanController had to change to support it.
 */
@Component("groq")
public class GroqPlanProvider implements PlanGenerationProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final WorkoutPlanPromptBuilder promptBuilder;

    public GroqPlanProvider(
            @Value("${ai.groq.base-url}") String baseUrl,
            @Value("${ai.groq.api-key}") String apiKey,
            @Value("${ai.groq.model}") String model,
            ObjectMapper objectMapper,
            WorkoutPlanPromptBuilder promptBuilder
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public GeneratedPlan generatePlan(Intake intake) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GROQ_API_KEY is not set. Get a free key at https://console.groq.com/keys "
                            + "and add it to backend/.env before starting the backend.");
        }

        String prompt = promptBuilder.build(intake, "Return ONLY valid JSON matching this structure, no other text, no markdown fences.");

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "response_format", Map.of("type", "json_object")
        );

        JsonNode response;
        try {
            response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "Groq API call failed (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        }

        if (response == null) {
            throw new IllegalStateException("Groq API returned an empty response");
        }

        String rawJson = response
                .path("choices").path(0)
                .path("message").path("content")
                .asText();

        try {
            return objectMapper.readValue(rawJson, GeneratedPlan.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "AI response did not match the expected plan schema: " + e.getMessage(), e);
        }
    }

}
