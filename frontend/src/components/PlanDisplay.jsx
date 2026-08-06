import React from "react";

export default function PlanDisplay({ planResponse }) {
    if (!planResponse) return null;

    const { plan } = planResponse;

    return (
        <section>
            <h2>Your plan</h2>
            <p>{plan.durationWeeks}-week plan</p>

            {plan.days.map((day) => (
                <article key={day.dayNumber}>
                    <h3>
                        Day {day.dayNumber}: {day.focus}
                    </h3>
                    <ul>
                        {day.exercises.map((exercise, idx) => (
                            <li key={idx}>
                                <strong>{exercise.name}</strong> — {exercise.sets} sets x{" "}
                                {exercise.reps} reps, rest {exercise.restSeconds}s
                                {exercise.equipment && exercise.equipment !== "none" && (
                                    <> ({exercise.equipment})</>
                                )}
                                {exercise.notes && <p>{exercise.notes}</p>}
                            </li>
                        ))}
                    </ul>
                </article>
            ))}

            {plan.progressionNotes && (
                <footer>
                    <h4>Progression</h4>
                    <p>{plan.progressionNotes}</p>
                </footer>
            )}
        </section>
    );
}
