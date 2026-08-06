package com.vasco.workoutplan.service;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vasco.workoutplan.model.GeneratedPlan;
import com.vasco.workoutplan.model.Intake;
import com.vasco.workoutplan.model.PlanResponse;

/**
 * Orchestrates plan generation and storage.
 *
 * v1 note: storage is an in-memory map, not a database — kept simple while
 * cloud deployment (DynamoDB) is being learned. Swapping this for real
 * persistence later shouldn't require changes to the controller.
 *
 * Provider selection is config-driven via `ai.provider` (see application.yml).
 * Spring injects every PlanGenerationProvider bean into a Map keyed by its
 * @Component qualifier name ("gemini", "groq", ...) — the active one is
 * looked up by name, so adding a new provider never requires changing this
 * class, only adding a new @Component-annotated implementation.
 */
@Service
public class PlanService {

    private final PlanGenerationProvider provider;
    private final Map<UUID, PlanResponse> plans = new ConcurrentHashMap<>();

    public PlanService(
            Map<String, PlanGenerationProvider> providers,
            @Value("${ai.provider}") String activeProviderName
    ) {
        PlanGenerationProvider selected = providers.get(activeProviderName);
        if (selected == null) {
            throw new IllegalStateException(
                    "Unknown ai.provider '" + activeProviderName + "'. Available: " + providers.keySet());
        }
        this.provider = selected;
    }

    public PlanResponse createPlan(Intake intake) {
        GeneratedPlan generatedPlan = provider.generatePlan(intake);

        PlanResponse response = new PlanResponse(
                UUID.randomUUID(),
                Instant.now(),
                intake,
                generatedPlan
        );

        plans.put(response.planId(), response);
        return response;
    }

    public Optional<PlanResponse> getPlan(UUID planId) {
        return Optional.ofNullable(plans.get(planId));
    }

    public Collection<PlanResponse> getAllPlans() {
        return plans.values();
    }
}
