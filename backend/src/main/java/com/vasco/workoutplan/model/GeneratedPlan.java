package com.vasco.workoutplan.model;

import java.util.List;

/**
 * The structured plan body produced by the AI provider. This is what gets
 * validated against the schema before being persisted — see
 * PlanGenerationProvider and PlanService.
 */
public record GeneratedPlan(
        int durationWeeks,
        List<WorkoutDay> days,
        String progressionNotes
) {
}
