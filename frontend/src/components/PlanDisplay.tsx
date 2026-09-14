import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Chip from "@mui/material/Chip";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import formatDisplayValue from "../utils/formatDisplayValue";
import type { PlanResponse } from "../types";

interface PlanDisplayProps {
    planResponse: PlanResponse | null;
}

export default function PlanDisplay({ planResponse }: PlanDisplayProps) {
    if (!planResponse) return null;

    const { plan } = planResponse;

    return (
        <Stack spacing={3}>
            <Box>
                <Typography variant="h4">Your plan</Typography>
                <Typography variant="body1" color="text.secondary">
                    {plan.durationWeeks}-week training block • {plan.days.length} workout days
                </Typography>
            </Box>

            {plan.days.map((day) => (
                <Card key={day.dayNumber} sx={{ overflow: "hidden" }}>
                    <CardContent sx={{ p: { xs: 2, md: 3 } }}>
                        <Stack spacing={2}>
                            <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 1, flexWrap: "wrap" }}>
                                <Typography variant="h5">
                                    Day {day.dayNumber}
                                </Typography>
                                <Chip label={day.focus} color="primary" variant="filled" />
                            </Box>

                            <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: 1, overflowX: "auto" }}>
                                <Table size="small" aria-label={`Workout plan for day ${day.dayNumber}`}>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Exercise</TableCell>
                                            <TableCell>Sets</TableCell>
                                            <TableCell>Reps</TableCell>
                                            <TableCell>Rest</TableCell>
                                            <TableCell>Equipment</TableCell>
                                            <TableCell>Notes</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {day.exercises.map((exercise, idx) => (
                                            <TableRow key={`${day.dayNumber}-${idx}`}>
                                                <TableCell sx={{ fontWeight: 700 }}>{exercise.name}</TableCell>
                                                <TableCell>{exercise.sets}</TableCell>
                                                <TableCell>{exercise.reps}</TableCell>
                                                <TableCell>{exercise.restSeconds}s</TableCell>
                                                <TableCell>{exercise.equipment && exercise.equipment !== "none" ? formatDisplayValue(exercise.equipment) : "—"}</TableCell>
                                                <TableCell>{exercise.notes || "—"}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </Stack>
                    </CardContent>
                </Card>
            ))}

            {plan.progressionNotes && (
                <Alert severity="info" variant="outlined" sx={{ borderRadius: 3 }}>
                    <Typography variant="subtitle2" sx={{ mb: 0.5 }}>
                        Progression notes
                    </Typography>
                    {plan.progressionNotes}
                </Alert>
            )}
        </Stack>
    );
}
