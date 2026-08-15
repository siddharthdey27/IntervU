# PrepPilot — AI Interview Preparation Platform

LeetCode + ChatGPT + Resume Analyzer, in one app. Upload your resume, and get an
AI interviewer that asks personalized questions using RAG over your resume,
job descriptions, and company docs.

## Stack

| Layer      | Tech |
|------------|------|
| Frontend   | React (Vite) + Tailwind CSS (Vercel) |
| Backend    | Java Spring Boot, Spring Security (JWT) (Render) |
| Database   | Supabase (Postgres) |
| Vector DB  | pgvector (via Supabase Postgres extension) |
| AI         | LangChain4j + Gemini / OpenAI (chat + embeddings) |
| Deploy     | Docker on Render (Backend) + Vercel (Frontend) |

## 1. Set up Supabase

1. Create a project at https://supabase.com.
2. Go to **SQL Editor** and run `backend/src/main/resources/db/migration/V1__init.sql`.
   This enables the `vector` extension and creates all tables (users, resumes,
   resume_chunks, interview_sessions, etc).
3. Go to **Project Settings -> Database** and copy the connection string,
   username, and password — you'll need these for `SUPABASE_DB_URL`,
   `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`.

> Note: `ddl-auto: update` in `application.yml` will also let Hibernate manage
> non-vector tables automatically, but the `V1__init.sql` script is the source
> of truth for the `vector` columns and indexes — always run it first.

## 2. Configure environment variables

```bash
cp .env.example .env
# then fill in: Supabase creds, JWT_SECRET, GEMINI_API_KEY / OPENAI_API_KEY
```

## 3. Run locally (without Docker)

**Backend**
```bash
cd backend
export $(cat ../.env | xargs)   # or set env vars manually / via IDE run config
mvn spring-boot:run
```

**Frontend**
```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`, backend on `http://localhost:8080`.

## 4. Run with Docker Compose

```bash
docker compose --env-file .env up --build
```

## 5. Deploy to Production

- **Backend**: Deploy on [Render](https://render.com) using the `render.yaml` blueprint (Docker web service).
- **Frontend**: Deploy on [Vercel](https://vercel.com) with root directory set to `frontend` and `VITE_API_BASE_URL` pointing to your Render API URL + `/api`.
- See `RENDER.md` for complete step-by-step instructions.

## How the RAG pipeline works

1. **Upload**: PDF text is extracted directly in-memory via PDFBox (`PdfExtractionService`).
2. **Chunk + embed**: text is split into ~800-char overlapping chunks, each
   embedded via Gemini/OpenAI (`EmbeddingService`), and stored in
   `resume_chunks.embedding` (pgvector) via raw JDBC
   (`VectorStoreServiceImpl` — Hibernate can't map `vector` columns directly).
3. **Retrieve**: on each interview turn, the latest user message is embedded
   and a cosine-similarity search (`<=>` operator) pulls the top-k most
   relevant resume chunks + company/JD chunks (`RagService`).
4. **Generate**: retrieved context is injected into the system prompt sent to
   the chat model via LangChain4j (`ChatModelService`), which drives the
   interviewer persona (`InterviewService`).

## Project structure

```
backend/
  src/main/java/com/preppilot/
    config/       # Security, CORS, exception handling
    controller/    # REST endpoints (auth, resumes, interviews)
    dto/           # Request/response records
    entity/        # JPA entities
    repository/    # Spring Data repositories
    security/      # JWT filter + service
    service/       # Business logic (RAG, embeddings, chat, S3, PDF)
  src/main/resources/
    application.yml
    db/migration/V1__init.sql
frontend/
  src/
    api/          # axios clients
    context/      # AuthContext
    components/   # Navbar, ProtectedRoute
    pages/        # Login, Register, Dashboard, ResumeUpload, InterviewChat
```

## What's stubbed / next steps

This is a working v1 skeleton for the **core loop**: auth → resume upload →
RAG-driven text interview. Not yet implemented (see roadmap in project notes):

- **Coding questions module**: question bank + code editor (Monaco) + real
  code execution sandbox (e.g. Judge0). Currently only the DB tables exist.
- **Voice interview**: Whisper (STT) + TTS integration.
- **Progress dashboard charts**: the `user_progress_summary` SQL view exists;
  needs a `/api/progress` endpoint + Recharts UI.
- **Company-specific knowledge ingestion**: an admin endpoint to upload JD /
  company docs into `knowledge_documents` + `knowledge_chunks` (same
  chunk-and-embed pipeline as resumes, just needs a controller).
- Refresh-token rotation endpoint (`/api/auth/refresh`) — currently only
  issued, not yet consumed.
