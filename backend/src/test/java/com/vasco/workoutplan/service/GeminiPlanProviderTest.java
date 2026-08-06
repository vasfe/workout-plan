package com.vasco.workoutplan.service;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vasco.workoutplan.model.GeneratedPlan;
import com.vasco.workoutplan.model.Intake;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiPlanProviderTest {

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
        GeminiPlanProvider provider = new GeminiPlanProvider(
                "http://localhost",
                "",
                "dummy-model",
                new ObjectMapper(),
                new WorkoutPlanPromptBuilder()
        );

        Intake intake = new Intake(
                List.of("muscle gain"),
                Intake.ExperienceLevel.BEGINNER,
                3,
                List.of("none"),
                170,
                70
        );

        assertThatThrownBy(() -> provider.generatePlan(intake))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GEMINI_API_KEY is not set");
    }

    @Test
    void generatePlanParsesValidGeminiResponse() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/models/test-model:generateContent", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) {
                try {
                    assertThat(exchange.getRequestMethod()).isEqualTo("POST");
                    assertThat(exchange.getRequestURI().getQuery()).contains("key=test-key");

                    String rawJson = "{\"durationWeeks\":4,\"days\":[{\"dayNumber\":1,\"focus\":\"Upper Body\",\"exercises\":[{\"name\":\"Bench Press\",\"sets\":3,\"reps\":\"8-12\",\"restSeconds\":90,\"equipment\":\"bench\",\"notes\":\"Keep elbows tucked\"}]}],\"progressionNotes\":\"Add weight when the last set feels easy.\"}";
                    String responseBody = "{" +
                            "\"candidates\":[{" +
                            "\"content\":{\"parts\":[{\"text\":\"" + rawJson.replace("\"", "\\\"") + "\"}]}}]}";

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
        GeminiPlanProvider provider = new GeminiPlanProvider(
                "http://localhost:" + port,
                "test-key",
                "test-model",
                new ObjectMapper(),
                new WorkoutPlanPromptBuilder()
        );

        Intake intake = new Intake(
                List.of("muscle gain"),
                Intake.ExperienceLevel.INTERMEDIATE,
                4,
                List.of("dumbbells", "bench"),
                180,
                80
        );

        GeneratedPlan plan = provider.generatePlan(intake);

        assertThat(plan.durationWeeks()).isEqualTo(4);
        assertThat(plan.days()).hasSize(1);
        assertThat(plan.days().get(0).focus()).isEqualTo("Upper Body");
        assertThat(plan.progressionNotes()).contains("Add weight when the last set feels easy.");
    }
}
