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

Greenfield. The only source file is `src/main/java/org/example/Main.java`, a generated IntelliJ "Hello World" scaffold — a placeholder to be replaced by the Quarkus bootstrap (ROADMAP task `T-002`). No tests, no business logic, no Quarkus deps yet. Docs (above) lead the code for now.

## Build & Run

This project uses Maven but `pom.xml` declares no plugins, dependencies, or packaging — only the compiler source/target. There is no Maven wrapper script committed (`.mvn/` is empty), so use a system `mvn`.

- Build: `mvn compile`
- Package: `mvn package`
- Run (no `exec` plugin configured): compile then run directly, e.g. `java -cp target/classes org.example.Main`
- Tests: none exist and no test framework is on the classpath. Adding JUnit requires a `dependencies` block in `pom.xml` first.

## Key constraints

- **Java 25** (`maven.compiler.source`/`target` = 25). The code relies on Java 25 preview features: an instance/no-args `main()` method (no `public static void main(String[])`) and the implicit `IO.println` console API. Keep these in mind — running on an older JDK will fail, and adding a class with a classic `main` signature would diverge from the existing style.
- Base package is `org.example`; groupId `org.example`, artifactId `Horus`.
