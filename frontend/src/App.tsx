import { useState } from "react";
import IntakeForm from "./components/IntakeForm";
import PlanDisplay from "./components/PlanDisplay";
import { generatePlan } from "./api";
import type { Intake, PlanResponse } from "./types";

export default function App() {
    const [planResponse, setPlanResponse] = useState<PlanResponse | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleGenerate(intake: Intake) {
        setIsLoading(true);
        setError(null);
        try {
            const response = await generatePlan(intake);
            setPlanResponse(response);
        } catch (err) {
            setError(err instanceof Error ? err.message : "Failed to generate plan");
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <main>
            <h1>Workout Plan AI</h1>
            <IntakeForm onSubmit={handleGenerate} isLoading={isLoading} />
            {error && <p role="alert">{error}</p>}
            <PlanDisplay planResponse={planResponse} />
        </main>
    );
}
