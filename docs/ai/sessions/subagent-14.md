# Segéd: README ticket #14 (Claude Haiku)

Az első üzenet a fő AI által írt feladatleírás (brief).

### 🧠 Fő AI → segéd

As you work, call mcp__agent-dock__helper_progress with your task name and a percent at about 25, 50, 75 and 100. Do not call plan_steps. Use the task name "README #14".

## Goal
Implement GitHub issue #14 (README.md) in the private repo gchovanyecz/test. Read it: `gh issue view 14 --repo gchovanyecz/test`, plus #17 (overview + decisions) and skim the other closed issues (#1–#13) for context. `gh` is logged in; `origin` pushes over SSH; commit identity is in the repo config — don't change git config. Branch `feature/14-readme` from the latest `origin/main` (`git fetch origin` first). Finish with a PR containing `Closes #14`.

## What exists (main) — read the code, don't guess
- `backend/`: Spring Boot 4.1.2, Java 25, Maven wrapper, PostgreSQL 18, Flyway (`db/migration`, 5 seeded rooms), layered packages `room` and `booking` (controller → service → repository, `dto`, `domain`), `common/{config,exception,web}`. Endpoints: `GET /api/rooms`, `GET /api/rooms/{roomId}`, `GET /api/bookings?roomId&from&to`, `GET /api/bookings/{bookingId}`, `POST /api/bookings`, `DELETE /api/admin/bookings/{bookingId}`. Rules in `booking/service/BookingTimeRules` + `application.yml` `booking.rules.*`. Overlap: service check + PostgreSQL exclusion constraint (`V2__create_booking.sql`), errors as ProblemDetail (`GlobalExceptionHandler`).
- `frontend/`: Angular 22.2.4, Node 24 (`.nvmrc`), pages `/rooms`, `/bookings` (list + filter + booking form), `/admin` (delete with confirmation), own CSS, dev proxy.
- `docker-compose.yml` (db, backend :8080, frontend :4200 via nginx proxying `/api`), `backend/Dockerfile`, `frontend/Dockerfile`, `frontend/nginx.conf`.
- `docs/ai/README.md` and `docs/ai/prompt-log.md` (AI usage documentation).
- Current root `README.md` is a short stub; `frontend/README.md` exists.

## Write the root README.md (Hungarian)
Sections (concise, accurate, scannable — tables where they help):
1. Title + one-paragraph description and feature list.
2. Tech stack with the real versions.
3. **Indítás két paranccsal**: prerequisites (Git, Docker with Compose), then exactly `git clone git@github.com:gchovanyecz/test.git` (+ `cd test`) and `docker compose up --build`. Mention first build time and how to stop (`docker compose down`, `-v` to wipe data).
4. URLs: frontend http://localhost:4200, Swagger UI http://localhost:8080/swagger-ui.html, OpenAPI JSON, health.
5. API endpoints table (method, path, description, main status codes), noting admin endpoints under `/api/admin` and that there is no authentication by design.
6. Booking rules (15-min grid, 15 min–8 h, not in the past, no overlap, half-open intervals, UTC instants / ISO-8601 with offset) and error format (ProblemDetail examples for 400 and 409 — take them from real responses).
7. Architecture: backend layers and responsibilities, why overlap is guarded twice (service check for a descriptive 409 + DB exclusion constraint for concurrency), frontend structure, Docker topology (nginx proxy → no CORS).
8. Local development without Docker: backend (JDK 25; `./mvnw spring-boot:test-run` starts a throwaway Postgres via Testcontainers; `./mvnw verify`), frontend (`nvm use`, `npm ci`, `npm start` with proxy, `npm test`).
9. Tests: what is covered (critical overlap logic, rules, API, frontend unit tests) and how to run.
10. Project structure tree (short).
11. AI usage: link to `docs/ai/README.md` and `docs/ai/prompt-log.md`.
Keep `frontend/README.md` as is, but make sure nothing contradicts it.

## Verify (required — the README must be true)
- Actually run the documented Docker flow from your worktree: `docker compose up --build -d`, wait for healthy, then check the 4 URLs and a real POST + 409 + GET to capture realistic example responses; then `docker compose down -v`. Ports 8080 and 4200 are reserved for you during this task (other agents use other ports); if they are busy, report it instead of killing anything. Do not remove any other Docker containers/images/volumes.
- Run `cd backend && JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home ./mvnw -q verify` and the frontend `npm test` (Node 24: `~/.nvm/versions/node/v24.22.1/bin` or `nvm use 24`; the nvm default must stay 22) only if you need numbers for the README; don't invent numbers.
Only touch `README.md` (root). Do NOT modify code, Docker files, `frontend/README.md`, or `docs/ai/**` — other agents are working on Swagger (#7), tests (#8) and CI (#16) in parallel; don't document a CI badge.

## Finish
Hungarian commit message ending with `Closes #14` and the trailer `Co-Authored-By: Claude <your actual model name> <noreply@anthropic.com>`. Check `git status`; stage only README.md. Push, open a PR to `main` via `gh pr create --repo gchovanyecz/test`, title "#14 README.md", Hungarian body starting with `Closes #14`, summarizing sections and verification, ending with `🤖 Generated with [Claude Code](https://claude.com/claude-code)`.
Report back: the README outline, what you verified (with real outputs), the PR URL, and anything you could not verify.

### 🧠 Fő AI → segéd

<system-reminder>
Your final report is delivered through SubagentHandback: when your work is complete, call SubagentHandback({message: <your full report>}). The call ends your run, so make it your last step. Only a SubagentHandback call reaches your caller as your result; plain text you write at the end is not delivered. Do not also send the report with SendMessage: your caller would get it twice.
</system-reminder>

- `Bash` Check worktree state and read issues 14 and 17
- `Bash` Fetch origin, create feature branch, list repo files
- `Bash` Read closed issues 1 to 16 for context
  - ⚠️ *Hiba:* This agent is isolated in the worktree ~/Projects/vino/.claude/worktrees/agent-a8e19bd9634f7689c, but this command runs gh with the text .title + " [" + .state + "]\n" + .body inside a construct too complex to verify, so what it runs cannot be shown not to be git. Refusing to 
- `Bash` Read issues 13, 15 and 16
- `Bash` Read issues 1, 2, 4 and 5
- `Bash` Read AI docs and Docker files
  - ⚠️ *Hiba:* Exit code 1
- `Bash` Read Docker files and prompt log
- `Bash` Read backend config, pom and frontend package.json
- `Bash` Read controllers and exception handler
- `Bash` Read booking service, rules and DTOs
- `Bash` Read migrations and config classes
  - ⚠️ *Hiba:* Exit code 1
- `Bash` Check Docker daemon and port availability
- `Bash` Check Docker, ports, and frontend structure
  - ⚠️ *Hiba:* This agent is isolated in the worktree ~/Projects/vino/.claude/worktrees/agent-a8e19bd9634f7689c, but this command runs docker with the text {{.ServerVersion}} {{.OperatingSystem}} inside a construct too complex to verify, so what it runs cannot be shown not to be git. Refusin
- `Bash` Check Docker daemon and running containers
- `Bash` Check whether ports 8080 and 4200 are free
- `Bash` Build and start the Docker Compose stack detached
- `Bash` Check frontend test builder, proxy and Swagger config
  - ⚠️ *Hiba:* Exit code 1
- `Monitor` Compose backend health or build failure
  - ⚠️ *Hiba:* This agent is isolated in the worktree ~/Projects/vino/.claude/worktrees/agent-a8e19bd9634f7689c, but this command runs docker with the text {{.State.Health.Status}} inside a construct too complex to verify, so what it runs cannot be shown not to be git. Refusing to run it — a

### 🤖 Segéd

The Go-template braces in that health check tripped the worktree guard, so I'm switching to plain `docker compose ps` output.

- `Monitor` Compose backend health or build failure
- `Bash` Read the compose build output so far

### 🤖 Segéd

The stack came up and the backend reports healthy. Next I'll check the four URLs and capture real POST, 409 and GET responses.

- `Bash` Check frontend, Swagger, OpenAPI, health and rooms endpoints
- `Bash` Create a booking, trigger 409 and 400 responses
- `Bash` Verify Swagger, nginx proxy, GET, 404 and admin delete

### 🤖 Segéd

The API responses are confirmed. Before writing the README I want to check the half-open boundary claim (a booking ending at 10:00 and one starting at 10:00 should both succeed), then run the backend and frontend test suites in the background.

- `Bash` Check adjacent booking and other rule violations
- `Bash` Run backend mvn verify and log output to scratchpad

### 🤖 Segéd

The boundary and rule checks behave as documented. Next, the frontend tests under Node 24 in the background.

- `Bash` Run frontend npm ci and npm test with Node 24

### 🤖 Segéd

The test runs are going in the background. While they finish, I'm checking a few claims I plan to put in the README (test styles, admin confirmation, HttpClient usage).

- `Bash` Check test styles and frontend features used in code

### 🤖 Segéd

Both test runs finished. Next I'm collecting the actual test counts and the installed versions for the README.

- `Bash` Summarize backend and frontend test results
- `Bash` Read installed frontend versions and backend dependency versions
  - ⚠️ *Hiba:* Exit code 1
- `Bash` Read backend and frontend test logs with correct paths

### 🤖 Segéd

Both suites pass: backend 62 tests and frontend 77 tests in 11 files. Before I describe the test coverage in the README, I'm checking the actual test names.

- `Bash` List key backend test method names

### 🤖 Segéd

The test names support the coverage section. Now I'll read the current README (required before overwriting it) and write the new Hungarian version.

- `Read` ~/Projects/vino/.claude/worktrees/agent-a8e19bd9634f7689c/README.md
- `Write` ~/Projects/vino/.claude/worktrees/agent-a8e19bd9634f7689c/README.md

### 🤖 Segéd

The README is written. One sentence in the 409 example is wrong: the conflicting request was 09:30–10:30 and overlaps the 09:00–10:00 booking, so it was not the same time slot. Fixing that now.

- `Edit` ~/Projects/vino/.claude/worktrees/agent-a8e19bd9634f7689c/README.md

### 🤖 Segéd

Next I'm tearing down the stack with volumes, as the task requires, and checking that only this project's resources were removed.

- `Bash` Tear down compose stack and volumes for this project

### 🤖 Segéd

The stack and its volume are removed. One Testcontainers `ryuk` helper container is still running; it came from my `mvn verify` run and is not part of this project's compose stack, so I'll leave it alone and mention it in the report. Now I'm checking git status before committing.

- `Bash` Check git status and commit identity
- `Bash` Commit README in Hungarian and push branch
- `Bash` Open pull request to main with Hungarian body
  - ⚠️ *Hiba:* This agent is isolated in the worktree ~/Projects/vino/.claude/worktrees/agent-a8e19bd9634f7689c, but this command is too complex to verify that it stays inside the worktree. Refusing to run it — a worktree-isolated agent's git operations must target its own worktree. Split it

### 🤖 Segéd

The combined heredoc command was refused by the worktree guard, so I'm writing the PR body with the Write tool and then running `gh pr create` on its own.

- `Write` <scratchpad>/pr-14-body.md
- `Bash` Create pull request for feature/14-readme into main
- `SubagentHandback`

