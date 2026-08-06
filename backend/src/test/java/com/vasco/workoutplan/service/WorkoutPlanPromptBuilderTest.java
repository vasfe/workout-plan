package com.vasco.workoutplan.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vasco.workoutplan.model.Intake;

class WorkoutPlanPromptBuilderTest {

    private final WorkoutPlanPromptBuilder builder = new WorkoutPlanPromptBuilder();

    @Test
    void buildIncludesPromptStructureAndUserContextAndResponseInstruction() {
        Intake intake = new Intake(
                List.of("muscle gain", "fat loss"),
                Intake.ExperienceLevel.INTERMEDIATE,
                4,
                List.of("dumbbells", "bench"),
                180,
                80
        );

        String prompt = builder.build(intake, "no markdown fences");

        assertThat(prompt).contains("Generate a workout plan as JSON matching this exact structure:");
        assertThat(prompt).contains("\"durationWeeks\"");
        assertThat(prompt).contains("Goals: muscle gain, fat loss");
        assertThat(prompt).contains("Experience level: INTERMEDIATE");
        assertThat(prompt).contains("Days per week available: 4");
        assertThat(prompt).contains("Equipment available: dumbbells, bench");
        assertThat(prompt).contains("Height: 180cm, Weight: 80kg");
        assertThat(prompt).contains("no markdown fences");
    }
}
