-- Migration V3: Update vector dimensions from 1536 (OpenAI) to 768 (Gemini text-embedding-004)

-- 1. Drop existing vector indexes
drop index if exists resume_chunks_embedding_idx;
drop index if exists knowledge_chunks_embedding_idx;

-- 2. Alter embedding columns to vector(768)
alter table resume_chunks alter column embedding type vector(768);
alter table knowledge_chunks alter column embedding type vector(768);

-- 3. Re-create cosine similarity indexes
create index if not exists resume_chunks_embedding_idx
    on resume_chunks using ivfflat (embedding vector_cosine_ops) with (lists = 100);

create index if not exists knowledge_chunks_embedding_idx
    on knowledge_chunks using ivfflat (embedding vector_cosine_ops) with (lists = 100);
