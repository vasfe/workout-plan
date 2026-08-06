package com.vasco.workoutplan.model;

import java.util.List;

public record WorkoutDay(
        int dayNumber,
        String focus,
        List<Exercise> exercises
) {
}
