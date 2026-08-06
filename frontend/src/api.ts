import type { Intake, PlanResponse } from "./types";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

interface ErrorResponse {
    details?: string;
    error?: string;
}

export async function generatePlan(intake: Intake): Promise<PlanResponse> {
    const response = await fetch(`${API_BASE_URL}/api/plans`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(intake),
    });

    if (!response.ok) {
        const errorBody = (await response.json().catch(() => ({} as ErrorResponse))) as ErrorResponse;
        throw new Error(errorBody.details || errorBody.error || "Failed to generate plan");
    }

    return (await response.json()) as PlanResponse;
}
