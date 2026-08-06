package com.vasco.workoutplan.controller;

import java.util.Collection;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.vasco.workoutplan.model.Intake;
import com.vasco.workoutplan.model.PlanResponse;
import com.vasco.workoutplan.service.PlanService;

@RestController
@RequestMapping("/api/plans")
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:3000}")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<PlanResponse> generatePlan(@Valid @RequestBody Intake intake) {
        PlanResponse response = planService.createPlan(intake);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable UUID planId) {
        return planService.getPlan(planId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Collection<PlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }
}
