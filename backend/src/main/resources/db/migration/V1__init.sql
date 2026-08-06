-- Run this in the Supabase SQL editor (or via a migration tool) BEFORE starting the backend.

-- 1. Enable pgvector extension (Supabase supports this out of the box)
create extension if not exists vector;

-- 2. Users
create table if not exists users (
    id uuid primary key default gen_random_uuid(),
    full_name varchar(150) not null,
    email varchar(150) not null unique,
    password_hash text not null,
    role varchar(20) not null default 'USER',
    created_at timestamptz not null default now()
);

-- 3. Resumes
create table if not exists resumes (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    file_name varchar(255) not null,
    s3_key text not null,
    raw_text text,
    uploaded_at timestamptz not null default now()
);

-- 4. Resume chunks with embeddings (RAG source #1)
create table if not exists resume_chunks (
    id uuid primary key default gen_random_uuid(),
    resume_id uuid not null references resumes(id) on delete cascade,
    chunk_index int not null,
    content text not null,
    embedding vector(1536), -- matches text-embedding-3-small; adjust if using a different model
    created_at timestamptz not null default now()
);

create index if not exists resume_chunks_embedding_idx
    on resume_chunks using ivfflat (embedding vector_cosine_ops) with (lists = 100);

-- 5. Job descriptions / company documents (RAG source #2 and #3)
create table if not exists knowledge_documents (
    id uuid primary key default gen_random_uuid(),
    source_type varchar(30) not null, -- JOB_DESCRIPTION | COMPANY_DOC | PAST_INTERVIEW
    company_name varchar(150),
    title varchar(255),
    content text not null,
    created_at timestamptz not null default now()
);

create table if not exists knowledge_chunks (
    id uuid primary key default gen_random_uuid(),
    document_id uuid not null references knowledge_documents(id) on delete cascade,
    chunk_index int not null,
    content text not null,
    embedding vector(1536),
    created_at timestamptz not null default now()
);

create index if not exists knowledge_chunks_embedding_idx
    on knowledge_chunks using ivfflat (embedding vector_cosine_ops) with (lists = 100);

-- 6. Interview sessions
create table if not exists interview_sessions (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    resume_id uuid references resumes(id),
    session_type varchar(30) not null, -- TEXT | VOICE | CODING | SYSTEM_DESIGN
    topic varchar(150),
    company_name varchar(150),
    status varchar(20) not null default 'IN_PROGRESS', -- IN_PROGRESS | COMPLETED
    started_at timestamptz not null default now(),
    ended_at timestamptz
);

-- 7. Messages within a session (question/answer transcript)
create table if not exists interview_messages (
    id uuid primary key default gen_random_uuid(),
    session_id uuid not null references interview_sessions(id) on delete cascade,
    sender varchar(10) not null, -- AI | USER
    content text not null,
    feedback text,
    score int,
    created_at timestamptz not null default now()
);

-- 8. Coding questions bank
create table if not exists coding_questions (
    id uuid primary key default gen_random_uuid(),
    title varchar(255) not null,
    difficulty varchar(20) not null, -- EASY | MEDIUM | HARD
    topic varchar(100),
    prompt text not null,
    created_at timestamptz not null default now()
);

create table if not exists coding_submissions (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references users(id) on delete cascade,
    question_id uuid not null references coding_questions(id) on delete cascade,
    code text not null,
    language varchar(30) not null,
    ai_feedback text,
    score int,
    submitted_at timestamptz not null default now()
);

-- 9. Progress dashboard aggregate view
create or replace view user_progress_summary as
select
    u.id as user_id,
    count(distinct s.id) as total_sessions,
    count(distinct cs.id) as total_coding_submissions,
    avg(cs.score) as avg_coding_score
from users u
left join interview_sessions s on s.user_id = u.id
left join coding_submissions cs on cs.user_id = u.id
group by u.id;
