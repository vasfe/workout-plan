package com.vasco.workoutplan.model;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * User-provided context used to generate a plan. Captured via the structured
 * intake form (dropdowns, simple inputs) — see project_purpose.md user flow.
 */
public record Intake(
        @NotEmpty List<String> goals,
        @NotNull ExperienceLevel experienceLevel,
        @Min(1) @Max(7) int daysPerWeek,
        @NotEmpty List<String> equipment,
        @Min(50) @Max(250) int heightCm,
        @Min(20) @Max(300) int weightKg
) {
    public enum ExperienceLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
