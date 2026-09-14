package com.vasco.workoutplan.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private Map<String, Object> buildJsonSchemaResponseFormat() {
        Map<String, Object> exerciseSchema = new LinkedHashMap<>();
        exerciseSchema.put("type", "object");
        exerciseSchema.put("additionalProperties", false);
        exerciseSchema.put("properties", Map.of(
                "name", Map.of("type", "string"),
                "sets", Map.of("type", "integer"),
                "reps", Map.of("type", "string"),
                "restSeconds", Map.of("type", "integer"),
                "equipment", Map.of("type", "string"),
                "notes", Map.of("type", "string")
        ));
        exerciseSchema.put("required", List.of("name", "sets", "reps", "restSeconds", "equipment", "notes"));

        Map<String, Object> daySchema = new LinkedHashMap<>();
        daySchema.put("type", "object");
        daySchema.put("additionalProperties", false);
        daySchema.put("properties", Map.of(
                "dayNumber", Map.of("type", "integer"),
                "focus", Map.of("type", "string"),
                "exercises", Map.of("type", "array", "items", exerciseSchema)
        ));
        daySchema.put("required", List.of("dayNumber", "focus", "exercises"));

        Map<String, Object> planSchema = new LinkedHashMap<>();
        planSchema.put("type", "object");
        planSchema.put("additionalProperties", false);
        planSchema.put("properties", Map.of(
                "durationWeeks", Map.of("type", "integer"),
                "days", Map.of("type", "array", "items", daySchema),
                "progressionNotes", Map.of("type", "string")
        ));
        planSchema.put("required", List.of("durationWeeks", "days", "progressionNotes"));

        Map<String, Object> jsonSchema = new LinkedHashMap<>();
        jsonSchema.put("name", "workout_plan");
        jsonSchema.put("strict", true);
        jsonSchema.put("schema", planSchema);

        Map<String, Object> responseFormat = new LinkedHashMap<>();
        responseFormat.put("type", "json_schema");
        responseFormat.put("json_schema", jsonSchema);
        return responseFormat;
    }

    @Override
    public GeneratedPlan generatePlan(Intake intake) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GROQ_API_KEY is not set. Get a free key at https://console.groq.com/keys "
                            + "and add it to backend/.env before starting the backend.");
        }

        String basePrompt = promptBuilder.build(
                intake,
                "Return ONLY valid JSON matching the required schema exactly. Do not include markdown fences, explanations, or extra text."
        );

        String lastError = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            List<Map<String, String>> messages = new java.util.ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "You are a strict JSON generator. Return only a single valid JSON object that matches the provided schema exactly and nothing else."));
            if (attempt == 0) {
                messages.add(Map.of("role", "user", "content", basePrompt));
            } else {
                messages.add(Map.of("role", "user", "content",
                        basePrompt + "\n\nThe previous response was rejected by the JSON validator. Fix it and return only the JSON object. "
                                + "Validation error: " + lastError + "\nDo not add commentary, markdown fences, or any text outside the JSON payload."));
            }

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "temperature", 0,
                    "messages", messages,
                    "response_format", buildJsonSchemaResponseFormat()
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
                String payload = e.getResponseBodyAsString();
                boolean shouldRetryForValidation = attempt < 1
                        && payload != null
                        && (payload.contains("json_validate_failed") || payload.contains("Failed to validate JSON"));
                if (shouldRetryForValidation) {
                    lastError = payload;
                    continue;
                }
                throw new IllegalStateException(
                        "Groq API call failed (HTTP " + e.getStatusCode() + "): " + payload, e);
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
                if (attempt < 1) {
                    lastError = "Returned content could not be parsed as the expected plan JSON: " + e.getMessage();
                    continue;
                }
                throw new IllegalStateException(
                        "AI response did not match the expected plan schema: " + e.getMessage(), e);
            }
        }

        throw new IllegalStateException("Groq API failed to return a valid workout plan after retrying.");
    }

}
