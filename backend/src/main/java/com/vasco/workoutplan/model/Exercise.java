package com.vasco.workoutplan.model;

/**
 * A single exercise within a workout day. `reps` is a String ("8-12") rather
 * than an int to allow the AI to express ranges naturally.
 */
public record Exercise(
        String name,
        int sets,
        String reps,
        int restSeconds,
        String equipment,
        String notes
) {
}
