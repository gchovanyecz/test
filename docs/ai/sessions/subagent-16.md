# Segéd: CI workflow ticket #16 (Claude Haiku)

Az első üzenet a fő AI által írt feladatleírás (brief).

### 🧠 Fő AI → segéd

As you work, call mcp__agent-dock__helper_progress with your task name and a percent at about 25, 50, 75 and 100. Do not call plan_steps. Use the task name "CI workflow #16".

## Goal
Implement GitHub issue #16 ("(Opcionális) CI: GitHub Actions a tesztek futtatására") in the private repo gchovanyecz/test. Read it: `gh issue view 16 --repo gchovanyecz/test`. `gh` is logged in; `origin` pushes over SSH; commit identity is in the repo config — don't change git config. Branch `feature/16-ci` from the latest `origin/main` (`git fetch origin` first). Finish with a PR containing `Closes #16`.

## Repo facts (verify by reading)
- `backend/`: Spring Boot 4.1.2, Java 25 (Temurin), Maven wrapper `backend/mvnw`; `./mvnw verify` runs unit + Testcontainers (PostgreSQL 18) integration tests — Docker is available on GitHub's `ubuntu-latest` runners.
- `frontend/`: Angular 22.2.4, Node 24 (`frontend/.nvmrc`), `npm ci`, `npm test` (= `ng test --watch=false`, vitest), `npm run build`. Lockfile `frontend/package-lock.json`.
- `docker-compose.yml`, `backend/Dockerfile`, `frontend/Dockerfile`.

## What to build: `.github/workflows/ci.yml`
- Triggers: `push` to `main` and `pull_request`.
- Job `backend`: checkout, `actions/setup-java` (temurin, 25, maven cache), `./mvnw -B verify` in `backend`.
- Job `frontend`: checkout, `actions/setup-node` with `node-version-file: frontend/.nvmrc` and npm cache on `frontend/package-lock.json`, `npm ci`, `npm test`, `npm run build` in `frontend`.
- Job `docker`: `docker compose build` (just verifies the images build; no push).
- Use current major versions of the official actions (look them up: `gh api repos/actions/checkout/releases/latest --jq .tag_name` etc.), minimal permissions (`contents: read`), and `concurrency` to cancel superseded runs on the same ref.
Only touch `.github/**`. Do NOT modify code, Docker files, `README.md`, or `docs/ai/**` — other agents are working in parallel on Swagger (#7), tests (#8) and the README (#14).

## Verify (required)
Push the branch, open the PR, then watch the real run (`gh run list --repo gchovanyecz/test --branch feature/16-ci`, `gh run watch <id> --repo gchovanyecz/test --exit-status`). If a job fails, read the logs (`gh run view <id> --log-failed`), fix, push, and re-check until all jobs pass — or, if the failure is outside `.github/**` (e.g. a flaky test or a GitHub Actions billing/permission limit on the private repo), stop and report it precisely instead of changing other files. Report the real job results and durations.

## Finish
Hungarian commit message(s) ending with `Closes #16` and the trailer `Co-Authored-By: Claude <your actual model name> <noreply@anthropic.com>`. Stage only `.github/**` (never `git add -A`). PR via `gh pr create --repo gchovanyecz/test`, title "#16 CI: GitHub Actions a tesztek futtatására", Hungarian body starting with `Closes #16`, listing the jobs and the run result (with a link to the run), ending with `🤖 Generated with [Claude Code](https://claude.com/claude-code)`.
Report back: the workflow summary, action versions used, the actual run results with link, the PR URL, and anything not done.

### 🧠 Fő AI → segéd

<system-reminder>
Your final report is delivered through SubagentHandback: when your work is complete, call SubagentHandback({message: <your full report>}). The call ends your run, so make it your last step. Only a SubagentHandback call reaches your caller as your result; plain text you write at the end is not delivered. Do not also send the report with SendMessage: your caller would get it twice.
</system-reminder>

- `Bash` Check working directory and git state
- `Bash` Read GitHub issue 16
- `Bash` Fetch origin and inspect repo layout
- `Bash` Create feature branch and inspect build configs
- `Bash` Look up latest action release versions
  - ⚠️ *Hiba:* This agent is isolated in the worktree ~/Projects/vino/.claude/worktrees/agent-a6f04bdee9f7cb936, but this command runs gh with a value computed at runtime (the variable r) inside a construct too complex to verify, so what it runs cannot be shown not to be git. Refusing to run
- `Bash` Get latest actions/checkout release tag
- `Bash` Get latest actions/setup-java release tag
- `Bash` Get latest actions/setup-node release tag
- `Bash` Check mvnw executable bit and gitignore
- `Write` ~/Projects/vino/.claude/worktrees/agent-a6f04bdee9f7cb936/.github/workflows/ci.yml
- `Bash` Validate workflow YAML and check tooling
- `Bash` Stage workflow file and create commit
- `Bash` Push feature branch to origin
- `Bash` Open pull request for feature/16-ci
  - ⚠️ *Hiba:* This agent is isolated in the worktree ~/Projects/vino/.claude/worktrees/agent-a6f04bdee9f7cb936, but this command names git in a form too complex to verify that it stays inside the worktree. Refusing to run it — a worktree-isolated agent's git operations must target its own w
- `Write` <scratchpad>/pr16_body.md
- `Bash` Open pull request for feature/16-ci
- `Bash` List workflow runs for the branch
- `Bash` Watch the CI run until it finishes
- `Bash` Get run conclusion and job timings
- `Bash` Check backend job log for test results
- `Bash` Check frontend job log for test and build output
- `Write` <scratchpad>/pr16_body_final.md
- `Bash` Update PR body with run results and verify PR
- `SubagentHandback`

