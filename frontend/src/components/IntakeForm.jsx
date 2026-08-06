import React, { useState } from "react";

const GOAL_OPTIONS = ["muscle_gain", "fat_loss", "general_fitness"];
const EQUIPMENT_OPTIONS = ["none", "dumbbells", "resistance_bands", "pull_up_bar"];
const EXPERIENCE_OPTIONS = ["BEGINNER", "INTERMEDIATE", "ADVANCED"];

export default function IntakeForm({ onSubmit, isLoading }) {
    const [goals, setGoals] = useState([]);
    const [equipment, setEquipment] = useState([]);
    const [experienceLevel, setExperienceLevel] = useState("BEGINNER");
    const [daysPerWeek, setDaysPerWeek] = useState(3);
    const [heightCm, setHeightCm] = useState(175);
    const [weightKg, setWeightKg] = useState(75);

    function toggleValue(list, setList, value) {
        setList(
            list.includes(value) ? list.filter((v) => v !== value) : [...list, value]
        );
    }

    function handleSubmit(e) {
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
                    onChange={(e) => setExperienceLevel(e.target.value)}
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
                    onChange={(e) => setDaysPerWeek(e.target.value)}
                />
            </label>

            <label>
                Height (cm)
                <input
                    type="number"
                    value={heightCm}
                    onChange={(e) => setHeightCm(e.target.value)}
                />
            </label>

            <label>
                Weight (kg)
                <input
                    type="number"
                    value={weightKg}
                    onChange={(e) => setWeightKg(e.target.value)}
                />
            </label>

            <button type="submit" disabled={isLoading || goals.length === 0}>
                {isLoading ? "Generating..." : "Generate plan"}
            </button>
        </form>
    );
}
