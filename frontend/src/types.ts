export type ExperienceLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";

export interface Intake {
    goals: string[];
    experienceLevel: ExperienceLevel;
    daysPerWeek: number;
    equipment: string[];
    heightCm: number;
    weightKg: number;
}

export interface Exercise {
    name: string;
    sets: number;
    reps: string;
    restSeconds: number;
    equipment: string;
    notes: string;
}

export interface WorkoutDay {
    dayNumber: number;
    focus: string;
    exercises: Exercise[];
}

export interface GeneratedPlan {
    durationWeeks: number;
    days: WorkoutDay[];
    progressionNotes: string;
}

export interface PlanResponse {
    planId: string;
    createdAt: string;
    intake: Intake;
    plan: GeneratedPlan;
}
