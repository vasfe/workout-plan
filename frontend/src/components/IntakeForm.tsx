import { useState } from "react";
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

    function toggleValue(list: string[], setList: React.Dispatch<React.SetStateAction<string[]>>, value: string) {
        setList(list.includes(value) ? list.filter((v) => v !== value) : [...list, value]);
    }

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
            <fieldset>
                <legend>Goals</legend>
                {GOAL_OPTIONS.map((goal) => (
                    <label key={goal}>
                        <input
                            type="checkbox"
                            checked={goals.includes(goal)}
                            onChange={() => toggleValue(goals, setGoals, goal)}
                        />
                        {goal.replace("_", " ")}
                    </label>
                ))}
            </fieldset>

            <fieldset>
                <legend>Equipment available</legend>
                {EQUIPMENT_OPTIONS.map((item) => (
                    <label key={item}>
                        <input
                            type="checkbox"
                            checked={equipment.includes(item)}
                            onChange={() => toggleValue(equipment, setEquipment, item)}
                        />
                        {item.replace("_", " ")}
                    </label>
                ))}
            </fieldset>

            <label>
                Experience level
                <select
                    value={experienceLevel}
                    onChange={(e) => setExperienceLevel(e.target.value as ExperienceLevel)}
                >
                    {EXPERIENCE_OPTIONS.map((level) => (
                        <option key={level} value={level}>
                            {level}
                        </option>
                    ))}
                </select>
            </label>

            <label>
                Days per week
                <input
                    type="number"
                    min={1}
                    max={7}
                    value={daysPerWeek}
                    onChange={(e) => setDaysPerWeek(Number(e.target.value))}
                />
            </label>

            <label>
                Height (cm)
                <input
                    type="number"
                    value={heightCm}
                    onChange={(e) => setHeightCm(Number(e.target.value))}
                />
            </label>

            <label>
                Weight (kg)
                <input
                    type="number"
                    value={weightKg}
                    onChange={(e) => setWeightKg(Number(e.target.value))}
                />
            </label>

            <button type="submit" disabled={isLoading || goals.length === 0}>
                {isLoading ? "Generating..." : "Generate plan"}
            </button>
        </form>
    );
}
