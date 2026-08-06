package com.vasco.workoutplan.service;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.vasco.workoutplan.model.GeneratedPlan;
import com.vasco.workoutplan.model.Intake;

/**
 * Google AI Studio (Gemini) implementation of PlanGenerationProvider.
 * Free tier, no card required — see project_purpose.md for the provider
 * comparison that led to this default choice.
 *
 * Uses Gemini's structured output mode (responseMimeType: application/json)
 * so the model returns JSON matching our schema directly, rather than
 * free-text that has to be parsed loosely.
 */
@Component("gemini")
public class GeminiPlanProvider implements PlanGenerationProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final WorkoutPlanPromptBuilder promptBuilder;

    public GeminiPlanProvider(
            @Value("${ai.gemini.base-url}") String baseUrl,
            @Value("${ai.gemini.api-key}") String apiKey,
            @Value("${ai.gemini.model}") String model,
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
                    "GEMINI_API_KEY is not set. Get a free key at https://aistudio.google.com/apikey "
                            + "and set it as an environment variable before starting the backend.");
        }

        String prompt = promptBuilder.build(intake, "Return ONLY valid JSON matching this structure, no other text.");

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{Map.of("text", prompt)})
                },
                "generationConfig", Map.of(
                        "responseMimeType", "application/json"
                )
        );

        JsonNode response;
        try {
            response = restClient.post()
                    .uri("/models/{model}:generateContent?key={apiKey}", model, apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            // Surfaces Gemini's actual error (invalid key, quota exceeded, bad
            // request, etc.) instead of failing as an unhandled 500.
            throw new IllegalStateException(
                    "Gemini API call failed (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString(), e);
        }

        if (response == null) {
            throw new IllegalStateException("Gemini API returned an empty response");
        }

        String rawJson = response
                .path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asText();

        try {
            return objectMapper.readValue(rawJson, GeneratedPlan.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "AI response did not match the expected plan schema: " + e.getMessage(), e);
        }
    }

}
