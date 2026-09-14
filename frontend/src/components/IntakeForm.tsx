import { useState } from "react";
import Autocomplete from "@mui/material/Autocomplete";
import Button from "@mui/material/Button";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import formatDisplayValue from "../utils/formatDisplayValue";
import type { ExperienceLevel, Intake } from "../types";

const GOAL_OPTIONS = ["muscle_gain", "fat_loss", "general_fitness"];
const EQUIPMENT_OPTIONS = ["none", "dumbbells", "resistance_bands", "pull_up_bar"];
const EXPERIENCE_OPTIONS: ExperienceLevel[] = ["BEGINNER", "INTERMEDIATE", "ADVANCED"];

interface IntakeFormProps {
    onSubmit: (intake: Intake) => void | Promise<void>;
    isLoading: boolean;
}

export default function IntakeForm({ onSubmit, isLoading }: IntakeFormProps) {
    const [goals, setGoals] = useState<string[]>([]);
    const [equipment, setEquipment] = useState<string[]>([]);
    const [experienceLevel, setExperienceLevel] = useState<ExperienceLevel>("BEGINNER");
    const [daysPerWeek, setDaysPerWeek] = useState(3);
    const [heightCm, setHeightCm] = useState(175);
    const [weightKg, setWeightKg] = useState(75);

    function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        onSubmit({
            goals,
            experienceLevel,
            daysPerWeek: Number(daysPerWeek),
            equipment,
            heightCm: Number(heightCm),
            weightKg: Number(weightKg),
        });
    }

    return (
        <form onSubmit={handleSubmit}>
            <Stack spacing={2}>
                <Autocomplete
                    multiple
                    options={GOAL_OPTIONS}
                    value={goals}
                    onChange={(_, value) => setGoals(value)}
                    getOptionLabel={(option) => formatDisplayValue(option)}
                    isOptionEqualToValue={(option, value) => option === value}
                    renderInput={(params) => (
                        <TextField
                            {...params}
                            label="Goals"
                            placeholder="Select your goals"
                        />
                    )}
                />

                <Autocomplete
                    multiple
                    options={EQUIPMENT_OPTIONS}
                    value={equipment}
                    onChange={(_, value) => setEquipment(value)}
                    getOptionLabel={(option) => formatDisplayValue(option)}
                    isOptionEqualToValue={(option, value) => option === value}
                    renderInput={(params) => (
                        <TextField
                            {...params}
                            label="Equipment available"
                            placeholder="Select equipment"
                        />
                    )}
                />

                <TextField
                    select
                    label="Experience level"
                    value={experienceLevel}
                    onChange={(e) => setExperienceLevel(e.target.value as ExperienceLevel)}
                    slotProps={{ select: { native: true } }}
                >
                    {EXPERIENCE_OPTIONS.map((level) => (
                        <option key={level} value={level}>
                            {formatDisplayValue(level)}
                        </option>
                    ))}
                </TextField>

                <TextField
                    type="number"
                    label="Days per week"
                    slotProps={{ htmlInput: { min: 1, max: 7 } }}
                    value={daysPerWeek}
                    onChange={(e) => setDaysPerWeek(Number(e.target.value))}
                />

                <TextField
                    type="number"
                    label="Height (cm)"
                    value={heightCm}
                    onChange={(e) => setHeightCm(Number(e.target.value))}
                />

                <TextField
                    type="number"
                    label="Weight (kg)"
                    value={weightKg}
                    onChange={(e) => setWeightKg(Number(e.target.value))}
                />

                <Button type="submit" variant="contained" disabled={isLoading || goals.length === 0}>
                    {isLoading ? "Generating..." : "Generate plan"}
                </Button>
            </Stack>
        </form>
    );
}
