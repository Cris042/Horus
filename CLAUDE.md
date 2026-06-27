# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

**Horus** is an **AI-augmented observability platform** — the "all-seeing eye" over a distributed system. Its primary goal is to capture the **full lifecycle of every request and every SQL query**, aggregate **all error logs**, and use **AI (Claude)** to summarize, in natural language, what is happening across the application (state summaries, trace explanations, root-cause analysis, anomaly detection).

The **system being observed** is a medical-records microservices simulation (the reference workload): Python load test (FastAPI + Locust) → Load Balancer (NGINX/Traefik) → three Quarkus services (Prontuário, Payment, Invoice), each with its own PostgreSQL → RabbitMQ (reports only) → Rust report/email worker → OpenTelemetry/Jaeger → Docker/Kubernetes.

The design is fully documented in **`docs/`** and **`lib.md`** — read these before implementing:
- `docs/PRD.md` — scope, functional (`RF-*`/`RF-H-*`) and non-functional (`RNF-*`/`RNF-H-*`) requirements, architecture diagram, acceptance criteria. `*-H-*` IDs are the Horus (AI observability) focus.
- `docs/adr/` — 12 Architecture Decision Records. ADR-0008..0011 + ADR-0013 (SAGA) are the new Horus decisions; ADR-0001..0005, 0007, 0012 are inherited from the source spec. (ADR-0006 "no SAGA" was removed when SAGA was adopted — see ADR-0013; numbering intentionally skips 0006.)
- `docs/ROLES.md` — RBAC roles (human + machine + AI-agent roles).
- `docs/ROADMAP.md` — phased plan broken into tasks (`T-xxx`) with requirement traceability and the critical path.
- `lib.md` — every dependency and target version, per component.

Source of truth for the original spec: a Portuguese PDF (*Documento Consolidado de Escopo e Requisitos, v2.0*) on the user's desktop; its requirements are transcribed into `docs/PRD.md`.

## Workflow & state — MUST FOLLOW

This repo uses an explicit governance flow. **Before doing any work, read [`docs/WORKFLOW.md`](docs/WORKFLOW.md) and [`state.md`](state.md).**

Flow: **`roadmap → task → PRD → execution plan → branch → PR → merge`**
- Each roadmap task (`T-xxx` in `docs/ROADMAP.md`) gets a folder `docs/tasks/T-xxx/` with a **`PRD.md`** (goal, acceptance criteria) and a **`PLAN.md`** (file-by-file execution plan). Copy from `docs/tasks/_TEMPLATE/`.
- **One task = one branch (`task/T-xxx-<slug>`) = one PR.** Never commit task work directly to `main`.

Two mandatory upkeep rules (see WORKFLOW.md / `docs/ROLES.md` §5):
- **R1 — update [`state.md`](state.md) on every delivery** (last delivery, next action, log).
- **R2 — update the task's `PLAN.md` whenever you create/modify a file in it** (status `⬜→✅`, date, change log). Add unplanned files to the table before touching them.

> These are conventions the executing agent must uphold; the harness does not enforce them automatically. Keep them current so project state is always reconstructable from `state.md` + the `PLAN.md` files.

## Current code status

The Maven build is a parent/aggregator at the repo root (`pom.xml`, `packaging=pom`) with one module so far: **`horus/`** — the Quarkus bootstrap of the Horus platform (task `T-002`). It exposes a smoke endpoint `GET /horus/info` plus SmallRye health at `/q/health`, with a `@QuarkusTest` smoke test. No domain logic yet — the domain services (`services/`), worker (`worker/`) and load test (`loadtest/`) are still scaffolding (see their READMEs). Docs (above) lead the code.

## Build & Run

The root `pom.xml` is a parent/aggregator (Java 25, Quarkus BOM in `dependencyManagement`); the buildable app lives in the `horus/` module. No Maven wrapper is committed yet (`.mvn/` is empty), so use a system `mvn` (3.9+). Build the whole tree from the root, or the module directly.

- Dev mode (live reload): `mvn -pl horus quarkus:dev` (or `cd horus && mvn quarkus:dev`)
- Build + test: `mvn package` (root, all modules) or `mvn -pl horus -am package`
- Run packaged: `java -jar horus/target/quarkus-app/quarkus-run.jar`
- Tests: JUnit 5 + REST Assured via Quarkus (`@QuarkusTest`); `mvn test`.
- Smoke once running: `GET http://localhost:8080/horus/info`, `GET /q/health`.

> Some environments here have no Maven/network — pin versions per `lib.md` and run the real build in CI or a dev box with internet.

## Key constraints

- **Java 25** (`maven.compiler.release = 25`, set once in the parent `pom.xml`). The old placeholder used Java 25 **preview** features (instance `main`, implicit `IO.println`); the Quarkus bootstrap **dropped them** (T-002) — Quarkus owns the entry point, so no `--enable-preview` is needed. If Quarkus 3.20 turns out not to support JDK 25, the fallback is Java 21 (LTS): change `maven.compiler.release` in the parent only (see `lib.md` and `docs/tasks/T-002/PRD.md`).
- Base package `org.example`; the Horus module uses `org.example.horus`. Maven coordinates: parent `org.example:horus-parent`, app module `org.example:horus`.
