# PrepPilot — AI Interview Preparation Platform

LeetCode + ChatGPT + Resume Analyzer, in one app. Upload your resume, and get an
AI interviewer that asks personalized questions using RAG over your resume,
job descriptions, and company docs.

## Stack

| Layer      | Tech |
|------------|------|
| Frontend   | React (Vite) + Tailwind CSS |
| Backend    | Java Spring Boot, Spring Security (JWT) |
| Database   | Supabase (Postgres) |
| Vector DB  | pgvector (via Supabase Postgres extension) |
| AI         | LangChain4j + OpenAI (chat + embeddings) |
| Storage    | AWS S3 (resume PDFs) |
| Deploy     | Docker, Nginx, GitHub Actions -> EC2 |

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
# then fill in: Supabase creds, JWT_SECRET, OPENAI_API_KEY, AWS S3 creds
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

Frontend served on port 80, backend API on port 8080.

## 5. Deploy to AWS EC2

1. Launch an EC2 instance (Ubuntu 22.04), install Docker + Docker Compose.
2. Clone the repo into `/opt/prep-pilot` and add your `.env` file there.
3. Add these GitHub Actions secrets: `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`.
4. Push to `main` — `.github/workflows/deploy.yml` builds both services and
   SSHes into EC2 to `docker compose up -d --build`.
5. Point Nginx (or an EC2 security-group + Route53 record) at the box; the
   frontend container already runs its own Nginx on port 80.

## How the RAG pipeline works

1. **Upload**: PDF → S3 (`S3StorageService`) + text extracted via PDFBox
   (`PdfExtractionService`).
2. **Chunk + embed**: text is split into ~800-char overlapping chunks, each
   embedded via OpenAI's `text-embedding-3-small` (`EmbeddingService`), and
   stored in `resume_chunks.embedding` (pgvector) via raw JDBC
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
