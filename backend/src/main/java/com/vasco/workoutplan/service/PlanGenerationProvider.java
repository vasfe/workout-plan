package com.vasco.workoutplan.service;

import com.vasco.workoutplan.model.GeneratedPlan;
import com.vasco.workoutplan.model.Intake;

/**
 * Provider-agnostic interface for generating a workout plan from an AI model.
 *
 * Each implementation (Gemini now, Claude/others later) is responsible for its
 * own request/response translation internally, so business logic (PlanService,
 * PlanController) never depends on a specific provider's API shape.
 *
 * Which implementation is active is config-driven — see application.yml
 * (`ai.provider`) and AiProviderConfig — not hardcoded.
 */
public interface PlanGenerationProvider {

    /**
     * Generates a structured, schema-conformant workout plan from the given
     * intake. Implementations must validate/parse the raw model output into
     * a GeneratedPlan before returning — callers should never see raw AI text.
     */
    GeneratedPlan generatePlan(Intake intake);
}
