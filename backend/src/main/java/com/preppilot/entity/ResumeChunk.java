package com.preppilot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * NOTE: the `embedding vector(1536)` column is intentionally NOT mapped here.
 * Hibernate doesn't support pgvector's `vector` type out of the box, so all
 * inserts/similarity-search reads for embeddings go through
 * {@link com.preppilot.service.impl.VectorStoreServiceImpl} using JdbcTemplate
 * with raw SQL (see RagServiceImpl). This entity is used for simple CRUD
 * (listing chunks, cascading deletes) where the vector itself isn't needed.
 */
@Entity
@Table(name = "resume_chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeChunk {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "resume_id", nullable = false)
    private UUID resumeId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
