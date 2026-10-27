# Segéd: Admin view ticket #12 (Claude Haiku)

Az első üzenet a fő AI által írt feladatleírás (brief).

### 🧠 Fő AI → segéd

As you work, call mcp__agent-dock__helper_progress with your task name and a percent at about 25, 50, 75 and 100. Do not call plan_steps. Use the task name "Admin view #12".

## Goal
Implement GitHub issue #12 ("Frontend: admin nézet, foglalás törlése") in the private repo gchovanyecz/test. Read it first: `gh issue view 12 --repo gchovanyecz/test` (plus #5 and #17 for decisions). `gh` is logged in; `origin` pushes over SSH and is configured; commit identity is in the repo config — do not change git config. Branch `feature/12-admin-view` from the latest `origin/main` (`git fetch origin` first); finish with a PR containing `Closes #12`.

## Environment
Node 24 via nvm (default must stay 22): `export NVM_DIR="$HOME/.nvm"; . "$NVM_DIR/nvm.sh"; nvm use 24 >/dev/null; ...` in each command. If sourcing nvm is blocked, use the Node 24 binary from `~/.nvm/versions/node/v24.22.1/bin` by absolute path. Install nothing globally.

## Backend contract
- `GET /api/bookings` (optional `roomId`) → `BookingResponse[]` sorted by start — being implemented right now in ticket #5 by another agent; code against the contract.
- `DELETE /api/admin/bookings/{id}` → 204; 404 ProblemDetail if it does not exist — also part of #5, not merged yet.
- `BookingResponse { id, roomId, roomName, title: string|null, startTime, endTime, createdAt }` (ISO instants).

## Existing frontend (main, `frontend/`, Angular 22.2.4)
Read it first: `BookingApiService` (`list()`, `adminDelete(id)`), `RoomApiService`, a global `errorInterceptor` that toasts every failed request, `NotificationService` (`error`/`info`), the placeholder `features/admin/admin.component.ts` at route `/admin`, the bookings page `features/bookings` (table, loading/empty/error states, `shared/load-status.ts`) and `core/format/booking-format.ts` (`displayTitle`, `formatBookingDate`, `formatTimeRange`) — reuse these helpers and the look of the bookings table for consistency. Decisions: own CSS only (no component library), Hungarian UI, standalone + signals + `inject()` + `@if/@for` + OnPush.

## What to build
1. Replace the admin placeholder with an admin page: a clear "Adminisztráció" heading/notice that this is the admin area; the list of all bookings (title, room, date, time range, created at), with loading/empty/error states.
2. Delete with confirmation: a "Törlés" button per row opens an own, accessible confirm dialog (native `<dialog>` element or a small own component; no `window.confirm`) showing which booking will be deleted; confirming calls `adminDelete(id)`; while deleting, disable the buttons for that row.
3. On success: info notification ("Foglalás törölve.") and refresh the list (or remove the row and then refresh). On error: the global interceptor already shows a toast, so don't show a second one; just keep the row and re-enable the buttons.
Do NOT touch `features/bookings/**`, `core/http/error.interceptor.ts` (another agent is changing the bookings page and the interceptor for #11 in parallel), `backend/`, Docker files, `docs/ai/prompt-log.md`, or root `README.md`. If you think a shared helper must change, put the new helper in a new file instead.

## Tests and verification (required)
Unit tests (vitest via `ng test`): list renders, empty state, confirm dialog shows the selected booking, cancel does not call the API, confirm calls `DELETE /api/admin/bookings/{id}` and reloads, failed delete keeps the row. `npm test` (non-watch) and `npm run build` must pass — report the real numbers.

## Finish
Hungarian commit message ending with `Closes #12` and the trailer `Co-Authored-By: Claude <your actual model name> <noreply@anthropic.com>` (use your real model name). Check `git status` and stage only intended files (never `git add -A`). Push and open a PR to `main` via `gh pr create --repo gchovanyecz/test`, title "#12 Frontend: admin nézet, foglalás törlése", Hungarian body starting with `Closes #12`, listing changes and verification results, ending with `🤖 Generated with [Claude Code](https://claude.com/claude-code)`.
Report back: files changed, dialog approach, test/build numbers, the PR URL, and anything not done or not verified.

### 🧠 Fő AI → segéd

<system-reminder>
Your final report is delivered through SubagentHandback: when your work is complete, call SubagentHandback({message: <your full report>}). The call ends your run, so make it your last step. Only a SubagentHandback call reaches your caller as your result; plain text you write at the end is not delivered. Do not also send the report with SendMessage: your caller would get it twice.
</system-reminder>

- `Bash` Check repo state and remotes in the worktree
- `Bash` Read issues 12, 5 and 17 from GitHub
- `Bash` Fetch origin and list frontend files on main
- `Bash` Create feature branch and read key frontend source files
  - ⚠️ *Hiba:* This agent is isolated in the worktree ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8, but this command names git in a form too complex to verify that it stays inside the worktree. Refusing to run it — a worktree-isolated agent's git operations must target its own w
- `Bash` Create feature branch from origin/main in worktree
- `Bash` Read API services, interceptor, notifications, routes
- `Bash` Read booking model, format helpers and bookings page
- `Bash` Read package.json, global styles, bookings spec and toast
- `Bash` Read existing spec patterns and angular config
- `Bash` Install frontend dependencies with Node 24 via npm ci
  - ⚠️ *Hiba:* This agent is isolated in the worktree ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8, but this command runs a string through ., which can't be verified to stay inside the worktree. Refusing to run it — a worktree-isolated agent's git operations must target its own 
- `Bash` Check Node 24 binary exists and its version
- `Bash` Install frontend dependencies with Node 24 on PATH
  - ⚠️ *Hiba:* This agent is isolated in the worktree ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8, but this command runs npm with a value computed at runtime (the variable PATH) inside a construct too complex to verify, so what it runs cannot be shown not to be git. Refusing to
- `Bash` Install frontend dependencies using Node 24 binary directly
- `Bash` Verify node_modules installed and test tools exist
- `Bash` Run baseline frontend unit tests on main
- `Bash` Check jsdom dialog support and rooms template
- `Bash` Write admin component TS and timestamp format helper
  - ⚠️ *Hiba:* This agent is isolated in the worktree ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8, but this command is too complex to verify that it stays inside the worktree. Refusing to run it — a worktree-isolated agent's git operations must target its own worktree. Split it
- `Write` ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8/frontend/src/app/core/format/timestamp-format.ts
- `Read` ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8/frontend/src/app/features/admin/admin.component.ts
- `Write` ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8/frontend/src/app/features/admin/admin.component.ts
- `Write` ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8/frontend/src/app/features/admin/admin.component.html
- `Read` ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8/frontend/src/app/features/admin/admin.component.ts
- `Write` ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8/frontend/src/app/features/admin/admin.component.css
- `Bash` Run production build to check compile errors
- `Write` ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8/frontend/src/app/features/admin/admin.component.spec.ts
- `Edit` ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8/frontend/src/app/features/admin/admin.component.spec.ts
- `Bash` Run frontend unit tests including new admin specs
- `Bash` Check Prettier formatting and rerun tests in another timezone
- `Bash` Show Prettier diffs for the new admin files
- `Bash` Apply Prettier formatting to new admin files
- `Bash` Rerun unit tests and production build after formatting
- `Bash` Run npm test and npm run build, then check git status
  - ⚠️ *Hiba:* This agent is isolated in the worktree ~/Projects/vino/.claude/worktrees/agent-a84f87a9a421b86e8, but this command changes directory to a location computed at runtime before running git. Refusing to run it — a worktree-isolated agent's git operations must target its own worktr
- `Bash` Run npm test and npm run build with Node 24
- `Bash` Check git status in the worktree and frontend gitignore
- `Bash` Run npm test and build with Node 24 first on PATH
- `Bash` Stage intended files and create the commit
- `Bash` Push feature branch to origin
- `Bash` Open pull request to main for issue 12
- `SubagentHandback`

