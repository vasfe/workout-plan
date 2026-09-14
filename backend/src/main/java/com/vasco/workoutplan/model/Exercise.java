package com.vasco.workoutplan.model;

/**
 * A single exercise within a workout day. `reps` is a String ("8-12") rather
 * than an int to allow the AI to express ranges naturally. `equipment` is also
 * kept as a free-form String so the model can recommend realistic names such as
 * "dumbbells", "bench", "kettlebells", "barbell", or "none".
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
