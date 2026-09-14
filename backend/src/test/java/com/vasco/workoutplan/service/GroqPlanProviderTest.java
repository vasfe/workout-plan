package com.vasco.workoutplan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.vasco.workoutplan.controller.GlobalExceptionHandler;
import com.vasco.workoutplan.model.GeneratedPlan;
import com.vasco.workoutplan.model.Intake;

class GroqPlanProviderTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    @Test
    void generatePlanThrowsWhenApiKeyMissing() {
        GroqPlanProvider provider = new GroqPlanProvider(
                "http://localhost",
                "",
                "dummy-model",
                new ObjectMapper(),
                new WorkoutPlanPromptBuilder()
        );

        Intake intake = new Intake(
                List.of("strength"),
                Intake.ExperienceLevel.ADVANCED,
                5,
                List.of("barbell"),
                175,
                85
        );

        assertThatThrownBy(() -> provider.generatePlan(intake))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GROQ_API_KEY is not set");
    }

    @Test
    void generatePlanUsesStructuredJsonRequestForGroq() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) {
                try {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    assertThat(requestBody).contains("\"temperature\":0");
                    assertThat(requestBody).contains("\"role\":\"system\"");
                    assertThat(requestBody).contains("Return ONLY valid JSON");
                    assertThat(requestBody).contains("\"response_format\"");
                    assertThat(requestBody).contains("\"type\":\"json_schema\"");
                    assertThat(requestBody).contains("\"strict\":true");

                    String rawJson = "{\"durationWeeks\":6,\"days\":[{\"dayNumber\":1,\"focus\":\"Lower Body\",\"exercises\":[{\"name\":\"Squat\",\"sets\":4,\"reps\":\"5-8\",\"restSeconds\":120,\"equipment\":\"barbell\",\"notes\":\"Drive through heels\"}]}],\"progressionNotes\":\"Increase weight carefully over 6 weeks.\"}";
                    String responseBody = "{" +
                            "\"choices\":[{" +
                            "\"message\":{\"content\":\"" + rawJson.replace("\"", "\\\"") + "\"}}]}";

                    byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        server.start();

        GroqPlanProvider provider = new GroqPlanProvider(
                "http://localhost:" + server.getAddress().getPort(),
                "test-key",
                "test-model",
                new ObjectMapper(),
                new WorkoutPlanPromptBuilder()
        );

        provider.generatePlan(new Intake(
                List.of("strength"),
                Intake.ExperienceLevel.ADVANCED,
                5,
                List.of("barbell"),
                175,
                85
        ));
    }

    @Test
    void generatePlanRetriesWhenGroqRejectsJsonValidation() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        final int[] requests = {0};
        server.createContext("/chat/completions", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) {
                try {
                    requests[0]++;
                    if (requests[0] == 1) {
                        String errorBody = "{\"error\":{\"message\":\"Failed to validate JSON. Please adjust your prompt.\",\"type\":\"invalid_request_error\",\"code\":\"json_validate_failed\"}}";
                        byte[] bytes = errorBody.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }
                        return;
                    }

                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    assertThat(requestBody).contains("Failed to validate JSON");

                    String rawJson = "{\"durationWeeks\":6,\"days\":[{\"dayNumber\":1,\"focus\":\"Lower Body\",\"exercises\":[{\"name\":\"Squat\",\"sets\":4,\"reps\":\"5-8\",\"restSeconds\":120,\"equipment\":\"barbell\",\"notes\":\"Drive through heels\"}]}],\"progressionNotes\":\"Increase weight carefully over 6 weeks.\"}";
                    String responseBody = "{" +
                            "\"choices\":[{" +
                            "\"message\":{\"content\":\"" + rawJson.replace("\"", "\\\"") + "\"}}]}";

                    byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        server.start();

        GroqPlanProvider provider = new GroqPlanProvider(
                "http://localhost:" + server.getAddress().getPort(),
                "test-key",
                "test-model",
                new ObjectMapper(),
                new WorkoutPlanPromptBuilder()
        );

        GeneratedPlan plan = provider.generatePlan(new Intake(
                List.of("strength"),
                Intake.ExperienceLevel.ADVANCED,
                5,
                List.of("barbell"),
                175,
                85
        ));

        assertThat(requests[0]).isEqualTo(2);
        assertThat(plan.durationWeeks()).isEqualTo(6);
    }

    @Test
    void globalExceptionHandlerSanitizesJsonValidationErrorsForUsers() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<java.util.Map<String, Object>> response = handler.handleProviderError(
                new IllegalStateException(
                        "Groq API call failed (HTTP 400): {\"error\":{\"message\":\"Failed to validate JSON. Please adjust your prompt.\",\"code\":\"json_validate_failed\"}}"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).containsEntry("error", "AI provider error");
        assertThat(response.getBody().get("details")).isEqualTo(
                "The AI could not generate a valid workout plan from your request. Please try again in a moment or simplify your goals, equipment, or schedule.");
        assertThat(response.getBody().get("details").toString()).doesNotContain("json_validate_failed");
        assertThat(response.getBody().get("details").toString()).doesNotContain("Failed to validate JSON");
    }

    @Test
    void generatePlanParsesValidGroqResponse() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) {
                try {
                    assertThat(exchange.getRequestMethod()).isEqualTo("POST");
                    assertThat(exchange.getRequestHeaders().getFirst("Authorization")).isEqualTo("Bearer test-key");

                    String rawJson = "{\"durationWeeks\":6,\"days\":[{\"dayNumber\":1,\"focus\":\"Lower Body\",\"exercises\":[{\"name\":\"Squat\",\"sets\":4,\"reps\":\"5-8\",\"restSeconds\":120,\"equipment\":\"barbell\",\"notes\":\"Drive through heels\"}]}],\"progressionNotes\":\"Increase weight carefully over 6 weeks.\"}";
                    String responseBody = "{" +
                            "\"choices\":[{" +
                            "\"message\":{\"content\":\"" + rawJson.replace("\"", "\\\"") + "\"}}]}";

                    byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        server.start();

        int port = server.getAddress().getPort();
        GroqPlanProvider provider = new GroqPlanProvider(
                "http://localhost:" + port,
                "test-key",
                "test-model",
                new ObjectMapper(),
                new WorkoutPlanPromptBuilder()
        );

        Intake intake = new Intake(
                List.of("strength"),
                Intake.ExperienceLevel.ADVANCED,
                5,
                List.of("barbell"),
                175,
                85
        );

        GeneratedPlan plan = provider.generatePlan(intake);

        assertThat(plan.durationWeeks()).isEqualTo(6);
        assertThat(plan.days()).hasSize(1);
        assertThat(plan.days().get(0).focus()).isEqualTo("Lower Body");
        assertThat(plan.progressionNotes()).contains("Increase weight carefully over 6 weeks.");
    }
}
