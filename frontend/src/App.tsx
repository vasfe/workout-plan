import { useState } from "react";
import Box from "@mui/material/Box";
import Container from "@mui/material/Container";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import Alert from "@mui/material/Alert";
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
        <Box
            sx={{
                minHeight: "100vh",
                background: "linear-gradient(180deg, #f5f8ff 0%, #eef7f4 100%)",
                py: { xs: 3, md: 6 },
            }}
        >
            <Container maxWidth="lg">
                <Stack spacing={3}>
                    <Box sx={{ textAlign: "center", px: 2 }}>
                        <Typography variant="h2" sx={{ mt: 1, mb: 1 }}>
                            AI workout plan creator
                        </Typography>
                        <Typography variant="body1" color="text.secondary">
                            Tailored workouts based on your goals, equipment, and training experience.
                        </Typography>
                    </Box>

                    <IntakeForm onSubmit={handleGenerate} isLoading={isLoading} />

                    {error && (
                        <Alert severity="error" variant="filled" role="alert">
                            {error}
                        </Alert>
                    )}

                    <PlanDisplay planResponse={planResponse} />
                </Stack>
            </Container>
        </Box>
    );
}
