package com.preppilot.service.impl;

import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;

/**
 * Handles all pgvector-specific reads/writes for resume_chunks, since Hibernate
 * can't map the `vector` column type directly. Uses Spring's JdbcTemplate with
 * the pgvector string format and explicit SQL vector casting.
 */
@Service
@RequiredArgsConstructor
public class VectorStoreServiceImpl {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VectorStoreServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;

    @jakarta.annotation.PostConstruct
    public void initSchema() {
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE resume_chunks ADD COLUMN IF NOT EXISTS embedding vector(768)");
        } catch (Exception e) {
            try {
                jdbcTemplate.execute("ALTER TABLE resume_chunks ADD COLUMN IF NOT EXISTS embedding text");
            } catch (Exception ignored) {}
        }
        try {
            jdbcTemplate.execute("ALTER TABLE knowledge_chunks ADD COLUMN IF NOT EXISTS embedding vector(768)");
        } catch (Exception e) {
            try {
                jdbcTemplate.execute("ALTER TABLE knowledge_chunks ADD COLUMN IF NOT EXISTS embedding text");
            } catch (Exception ignored) {}
        }
    }

    public void insertResumeChunk(UUID id, UUID resumeId, int chunkIndex, String content, float[] embedding) {
        String vecStr = new PGvector(embedding).getValue();
        try {
            jdbcTemplate.update("insert into resume_chunks (id, resume_id, chunk_index, content, created_at, embedding) values (?, ?, ?, ?, now(), ?::vector)",
                    id, resumeId, chunkIndex, content, vecStr);
        } catch (Exception e) {
            log.warn("Insert vector with cast failed: {}. Retrying with PGvector object.", e.getMessage());
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "insert into resume_chunks (id, resume_id, chunk_index, content, created_at, embedding) values (?, ?, ?, ?, now(), ?)");
                ps.setObject(1, id);
                ps.setObject(2, resumeId);
                ps.setInt(3, chunkIndex);
                ps.setString(4, content);
                ps.setObject(5, new PGvector(embedding));
                return ps;
            });
        }
    }

    public void insertKnowledgeChunk(UUID id, UUID documentId, int chunkIndex, String content, float[] embedding) {
        String vecStr = new PGvector(embedding).getValue();
        try {
            jdbcTemplate.update("insert into knowledge_chunks (id, document_id, chunk_index, content, created_at, embedding) values (?, ?, ?, ?, now(), ?::vector)",
                    id, documentId, chunkIndex, content, vecStr);
        } catch (Exception e) {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "insert into knowledge_chunks (id, document_id, chunk_index, content, created_at, embedding) values (?, ?, ?, ?, now(), ?)");
                ps.setObject(1, id);
                ps.setObject(2, documentId);
                ps.setInt(3, chunkIndex);
                ps.setString(4, content);
                ps.setObject(5, new PGvector(embedding));
                return ps;
            });
        }
    }

    /** Cosine-similarity search: returns the top-k most relevant chunk contents for a resume. */
    public List<String> findTopKChunksForResume(UUID resumeId, float[] queryEmbedding, int topK) {
        String sql = """
                select content
                from resume_chunks
                where resume_id = ?
                order by embedding <=> ?::vector
                limit ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("content"),
                resumeId, new PGvector(queryEmbedding).getValue(), topK);
    }

    /** Cosine-similarity search across all of a user's resumes (no single resume specified). */
    public List<String> findTopKChunksForUser(UUID userId, float[] queryEmbedding, int topK) {
        String sql = """
                select rc.content
                from resume_chunks rc
                join resumes r on r.id = rc.resume_id
                where r.user_id = ?
                order by rc.embedding <=> ?::vector
                limit ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("content"),
                userId, new PGvector(queryEmbedding).getValue(), topK);
    }

    public List<String> findTopKKnowledgeChunks(String companyName, float[] queryEmbedding, int topK) {
        String sql = """
                select kc.content
                from knowledge_chunks kc
                join knowledge_documents kd on kd.id = kc.document_id
                where (? is null or kd.company_name = ?)
                order by kc.embedding <=> ?::vector
                limit ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("content"),
                companyName, companyName, new PGvector(queryEmbedding).getValue(), topK);
    }
}
