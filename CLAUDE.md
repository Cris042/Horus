# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

**Horus** is an **AI-augmented observability platform** — the "all-seeing eye" over a distributed system. Its primary goal is to capture the **full lifecycle of every request and every SQL query**, aggregate **all error logs**, and use **AI (Claude)** to summarize, in natural language, what is happening across the application (state summaries, trace explanations, root-cause analysis, anomaly detection).

The **system being observed** is a medical-records microservices simulation (the reference workload): Python load test (FastAPI + Locust) → Load Balancer (NGINX/Traefik) → three Quarkus services (Prontuário, Payment, Invoice), each with its own PostgreSQL → a Quarkus SAGA orchestrator with its own PostgreSQL → RabbitMQ (reports only) → Rust report/email worker → OpenTelemetry/Jaeger/Loki/Prometheus → Docker/Kubernetes.

The design is fully documented in **`docs/`** and **`lib.md`** — read these before implementing:
- `docs/PRD.md` — scope, functional (`RF-*`/`RF-H-*`) and non-functional (`RNF-*`/`RNF-H-*`) requirements, architecture diagram, acceptance criteria. `*-H-*` IDs are the Horus (AI observability) focus.
- `docs/adr/` — 12 Architecture Decision Records. ADR-0008..0011 + ADR-0013 (SAGA) are the new Horus decisions; ADR-0001..0005, 0007, 0012 are inherited from the source spec. (ADR-0006 "no SAGA" was removed when SAGA was adopted — see ADR-0013; numbering intentionally skips 0006.)
- `docs/ROLES.md` — RBAC roles (human + machine + AI-agent roles).
- `docs/ROADMAP.md` — phased plan broken into tasks (`T-xxx`) with requirement traceability and the critical path.
- `docs/telemetry/CONTRACT.md` — the **telemetry contract** (T-005): canonical `service.name`s, W3C context over HTTP **and** RabbitMQ headers, span/log conventions, and PII rules. Read before any OTel/instrumentation work (Phases 4+).
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

The Maven build is a parent/aggregator at the repo root (`pom.xml`, `packaging=pom`) with five Java modules: **`horus/`**, **`services/prontuario/`**, **`services/payment/`**, **`services/invoice/`**, and **`services/saga-orchestrator/`**. The domain services implement the medical-records, payment, invoice and SAGA flows with Flyway migrations, isolated PostgreSQL credentials and OpenTelemetry instrumentation. `loadtest/` contains the FastAPI controller + Locust scenarios, and `worker/` contains the Rust RabbitMQ report/email worker with OTel context extraction.

Horus implements the full product: query adapters for Jaeger/Loki/Prometheus with **time-window search** (`/horus/traces`), `trace_id` correlation, request/query/error/service-map/SAGA lifecycle APIs, the AI layer on the **official Anthropic Java SDK** (per-tier models Haiku/Sonnet/Opus, runtime live/stub selection, mandatory prompt sanitization + audit trail, async jobs), anomaly detection + automatic alerts (webhook/e-mail), a static panel + waterfall UI, and JWT-based RBAC (on by default in `prod`). Roadmap phases 0–10 are delivered — see `state.md`.

Full stack locally: `make up-apps` (compose profile `apps`, LB on :8088, Horus on :8080) then `make e2e`. Kubernetes: `kubectl apply -k deploy/` (infra + apps).

## Build & Run

The root `pom.xml` is a parent/aggregator (Java 25, Quarkus BOM in `dependencyManagement`). A **Maven Wrapper is committed** (`./mvnw`, pinned to Maven 3.9.9 via `.mvn/wrapper/maven-wrapper.properties`), so builds are reproducible without a host `mvn` — prefer `./mvnw`. Build the whole tree from the root, or a module directly.

- Dev mode (live reload): `./mvnw -pl horus quarkus:dev`
- Build + test: `./mvnw verify` (root, all Java modules) or `./mvnw -pl horus -am verify`
- Run packaged: `java -jar horus/target/quarkus-app/quarkus-run.jar`
- Tests: JUnit 5 + REST Assured via Quarkus (`@QuarkusTest`); `./mvnw test`.
- Smoke once running: `GET http://localhost:8080/horus/info`, `GET /q/health`.

> **CI (T-004+):** `.github/workflows/ci.yml` runs Java (`./mvnw … verify`), Python (`pytest`) and Rust (`cargo fmt`, `clippy`, `test`) on every push to `main` and every PR. The first `./mvnw` call downloads Maven 3.9.9; if a host `~/.m2` has root-owned dirs, point at a fresh repo with `-Dmaven.repo.local=<dir>`.

## Key constraints

- **Java 25** (`maven.compiler.release = 25`, set once in the parent `pom.xml`). The old placeholder used Java 25 **preview** features (instance `main`, implicit `IO.println`); the Quarkus bootstrap **dropped them** (T-002) — Quarkus owns the entry point, so no `--enable-preview` is needed. **Quarkus platform = 3.37.0** (parent `pom.xml`): the earlier 3.20.0 ran fine for the persistence-free Horus bootstrap, but its **Panache entity enhancement could not read JDK 25 bytecode** (`Unsupported class file major version 69`), so **T-101 bumped Quarkus to 3.37.0** (a JDK-25-capable line; BUILD SUCCESS across all 5 modules). The Java 21 (LTS) fallback stays documented as a one-line escape hatch (change `maven.compiler.release` in the parent only; see `lib.md` and `docs/tasks/T-002/PRD.md`).
- Base package `org.example`; the Horus module uses `org.example.horus`. Maven coordinates: parent `org.example:horus-parent`, app module `org.example:horus`.
