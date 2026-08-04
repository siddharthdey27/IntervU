# Coding Questions Module — Setup

Everything here is free — no paid API keys or subscriptions required.

## 1. Backend (Java/Spring Boot)

1. Copy `backend/src/main/java/com/preppilot/{entity,repository,dto,service,controller}/*`
   into your existing `backend/src/main/java/com/preppilot/...` folders.
2. Copy `backend/src/main/resources/db/migration/V2__coding_questions.sql` into your
   existing migration folder and run it against Supabase (same as `V1__init.sql`).
3. Open `controller/CodeExecutionController.java` and fix the `extractUserId(...)`
   method — I don't have your actual `security/` package, so I stubbed it. Swap the
   `throw` for however you already pull the user ID out of the JWT elsewhere in your
   `InterviewService`/controllers (e.g. `((UserPrincipal) authentication.getPrincipal()).getId()`).
4. Add this to `application.yml`:
   ```yaml
   judge0:
     base-url: ${JUDGE0_BASE_URL:http://judge0-server:2358}
   ```
5. Make sure your Spring Boot app has the `spring-boot-starter-web` (for `RestTemplate`)
   and Jackson (already implied by Spring Boot) — no new dependencies needed.

## 2. Frontend (React)

1. Copy `frontend/src/pages/CodingQuestions.jsx`, `frontend/src/pages/CodeEditor.jsx`,
   and `frontend/src/api/codingApi.js` into your existing frontend.
2. Install Monaco (free, MIT license):
   ```bash
   npm install @monaco-editor/react
   ```
3. Add routes in your router:
   ```jsx
   <Route path="/coding-questions" element={<CodingQuestions />} />
   <Route path="/coding-questions/:id" element={<CodeEditor />} />
   ```
4. `codingApi.js` assumes you already have `src/api/axiosClient.js` with a JWT
   interceptor (matching the `src/api/` folder your README describes) — point the
   import at whatever that file is actually named.

## 3. Code execution sandbox (Judge0, self-hosted, free)

1. Copy `docker-compose.judge0.yml` and `judge0.conf` into your project root
   (next to your existing `docker-compose.yml`).
2. Run both compose files together:
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.judge0.yml up --build
   ```
3. That's it — your backend's `Judge0Service` talks to `http://judge0-server:2358`
   inside the Docker network, no API key needed. Judge0 currently supports Python,
   Java, and JavaScript (easy to add more languages — just add their ID to
   `Judge0Service.LANGUAGE_IDS`).

⚠️ Judge0's workers run submitted code in isolated sandboxes, but **don't expose
port 2358 to the public internet** without adding Judge0's built-in auth tokens —
it should only be reachable from your backend container.

## What you need to give me (all free)

- Nothing new, actually — this module only needs things you already have: Docker,
  your Supabase Postgres, and your existing JWT auth. If you want me to wire up
  `extractUserId(...)` exactly, paste the contents of your `security/` package
  (e.g. `JwtService.java`, `UserPrincipal.java`) and I'll finish that one method.

## Still stubbed after this

- Judge0 currently only maps Python/Java/JavaScript — add more languages by
  extending `LANGUAGE_IDS` in `Judge0Service.java` (full list at
  `https://github.com/judge0/judge0/blob/master/docs/api/README.md`).
- No admin UI for adding questions yet — insert directly into `coding_questions`
  via SQL for now (see the sample "Two Sum" row in the migration for the JSON shape).
