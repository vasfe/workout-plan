package com.vasco.workoutplan.model;

import java.time.Instant;
import java.util.UUID;

/**
 * The full record of a generated plan: what was asked for (intake) and what
 * came back (plan). Stored as a unit so past plans can be viewed later.
 */
public record PlanResponse(
        UUID planId,
        Instant createdAt,
        Intake intake,
        GeneratedPlan plan
) {
}
