import { createTheme } from "@mui/material/styles";

const theme = createTheme({
    palette: {
        mode: "light",
        primary: {
            main: "#2f6fed",
            light: "#dfeaff",
            dark: "#1d4ed8",
        },
        secondary: {
            main: "#0f766e",
            light: "#d9f8f4",
        },
        background: {
            default: "#f4f7fb",
            paper: "#ffffff",
        },
        text: {
            primary: "#132238",
            secondary: "#52657a",
        },
        warning: {
            main: "#f59e0b",
        },
        success: {
            main: "#16a34a",
        },
    },
    shape: {
        borderRadius: 18,
    },
    typography: {
        fontFamily: "Inter, 'Segoe UI', sans-serif",
        h1: {
            fontWeight: 800,
            letterSpacing: "-0.04em",
        },
        h2: {
            fontWeight: 700,
            letterSpacing: "-0.03em",
        },
        h3: {
            fontWeight: 700,
            letterSpacing: "-0.02em",
        },
        body1: {
            lineHeight: 1.6,
        },
    },
    components: {
        MuiPaper: {
            styleOverrides: {
                root: {
                    boxShadow: "0 10px 30px rgba(15, 23, 42, 0.08)",
                    border: "1px solid rgba(148, 163, 184, 0.18)",
                },
            },
        },
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 999,
                    textTransform: "none",
                    fontWeight: 700,
                    boxShadow: "none",
                },
            },
        },
        MuiChip: {
            styleOverrides: {
                root: {
                    borderRadius: 999,
                    fontWeight: 600,
                },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    borderRadius: 18,
                },
            },
        },
    },
});

export default theme;
