package com.vasco.workoutplan.service;

import org.springframework.stereotype.Component;

import com.vasco.workoutplan.model.Intake;

@Component
public class WorkoutPlanPromptBuilder {

    public String build(Intake intake, String responseInstruction) {
        return """
                Generate a workout plan as JSON matching this exact structure:
                {
                  "durationWeeks": <int>,
                  "days": [
                    {
                      "dayNumber": <int>,
                      "focus": "<string, e.g. Upper Body>",
                      "exercises": [
                        {
                          "name": "<string>",
                          "sets": <int>,
                          "reps": "<string, e.g. 8-12>",
                          "restSeconds": <int>,
                          "equipment": "<string, e.g. none, dumbbells>",
                          "notes": "<string>"
                        }
                      ]
                    }
                  ],
                  "progressionNotes": "<string>"
                }

                %s

                User context:
                - Goals: %s
                - Experience level: %s
                - Days per week available: %d
                - Equipment available: %s
                - Height: %dcm, Weight: %dkg
                """.formatted(
                responseInstruction,
                String.join(", ", intake.goals()),
                intake.experienceLevel(),
                intake.daysPerWeek(),
                String.join(", ", intake.equipment()),
                intake.heightCm(),
                intake.weightKg()
        );
    }
}
