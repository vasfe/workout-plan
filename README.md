# Workout Plan AI

AI-assisted workout plan generator. A structured intake form (goals,
equipment, experience level, etc.) is sent to an AI provider, which returns a
structured, schema-validated workout plan.

Built as a personal project to gain hands-on experience integrating AI into a
full-stack application via a provider-agnostic abstraction layer.

## Architecture

```
┌─────────────┐      POST /api/plans      ┌──────────────┐      ┌─────────────────┐
│ React + TS  │ ───────────────────────►  │  Spring Boot │ ───► │  AI Provider     │
│ (Vite)      │ ◄───────────────────────  │   backend    │ ◄─── │ (Groq, free      │
└─────────────┘     structured plan       └──────────────┘      │  tier for v1)    │
                                                                  └─────────────────┘
```

The backend depends only on the `PlanGenerationProvider` interface
(`backend/src/main/java/com/vasco/workoutplan/service/PlanGenerationProvider.java`),
not on any specific AI provider's API shape. **Groq is the default v1 provider**
(`GroqPlanProvider`) — Gemini's free tier excludes the UK/EU, so Groq is used
instead; a `GeminiPlanProvider` implementation also exists and can be selected
via `ai.provider: gemini` in `application.yml` if you're outside that region.
Which provider is active is config-driven, not hardcoded — see `PlanService`.

## v1 scope

Structured intake → AI plan generation → structured, validated output.
No free-text refinement, no persistence beyond an in-memory store, no session
feedback loop.

## Getting started

### Prerequisites
- Java 17+, Maven
- Node 20+ (frontend uses TypeScript + Vite)
- Docker (optional, for containerized run)
- A free Groq API key: https://console.groq.com/keys

### Run locally (without Docker)

Backend:
```bash
cd backend
cp .env.example .env
# edit .env and add your real GROQ_API_KEY
mvn spring-boot:run
```
The backend reads `backend/.env` automatically (via spring-dotenv) — no need to
export the variable manually each session. `.env` is already covered by
`.gitignore`, so it won't be committed.

Frontend (separate terminal):
```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:3000`, backend on `http://localhost:8080`.

### Run with Docker Compose

Uses the same `backend/.env` file as the non-Docker run above — no separate
`.env` needed at the repo root.

```bash
docker compose up --build
```

## API

- `POST /api/plans` — generate a plan from an `Intake` JSON body
- `GET /api/plans/{planId}` — retrieve a previously generated plan
- `GET /api/plans` — list all generated plans (in-memory, resets on restart)

## Project structure

```
.
├── backend/    Spring Boot API
├── frontend/   React + TypeScript app (Vite)
├── docker-compose.yml
└── .github/workflows/ci.yml   CI: build + test both services
```