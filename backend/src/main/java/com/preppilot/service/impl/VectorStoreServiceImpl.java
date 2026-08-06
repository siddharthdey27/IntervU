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
 * the pgvector-java helper to bind/parse `vector(1536)` values.
 */
@Service
@RequiredArgsConstructor
public class VectorStoreServiceImpl {

    private final JdbcTemplate jdbcTemplate;

    public void insertResumeChunk(UUID id, UUID resumeId, int chunkIndex, String content, float[] embedding) {
        jdbcTemplate.execute((PreparedStatement ignored) -> null); // no-op to keep lambda pattern consistent
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "insert into resume_chunks (id, resume_id, chunk_index, content, embedding) values (?, ?, ?, ?, ?)");
            ps.setObject(1, id);
            ps.setObject(2, resumeId);
            ps.setInt(3, chunkIndex);
            ps.setString(4, content);
            ps.setObject(5, new PGvector(embedding));
            return ps;
        });
    }

    /** Cosine-similarity search: returns the top-k most relevant chunk contents for a resume. */
    public List<String> findTopKChunksForResume(UUID resumeId, float[] queryEmbedding, int topK) {
        String sql = """
                select content
                from resume_chunks
                where resume_id = ?
                order by embedding <=> ?
                limit ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("content"),
                resumeId, new PGvector(queryEmbedding), topK);
    }

    /** Cosine-similarity search across all of a user's resumes (no single resume specified). */
    public List<String> findTopKChunksForUser(UUID userId, float[] queryEmbedding, int topK) {
        String sql = """
                select rc.content
                from resume_chunks rc
                join resumes r on r.id = rc.resume_id
                where r.user_id = ?
                order by rc.embedding <=> ?
                limit ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("content"),
                userId, new PGvector(queryEmbedding), topK);
    }

    public List<String> findTopKKnowledgeChunks(String companyName, float[] queryEmbedding, int topK) {
        String sql = """
                select kc.content
                from knowledge_chunks kc
                join knowledge_documents kd on kd.id = kc.document_id
                where (? is null or kd.company_name = ?)
                order by kc.embedding <=> ?
                limit ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("content"),
                companyName, companyName, new PGvector(queryEmbedding), topK);
    }
}
