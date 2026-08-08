package com.preppilot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocument {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "source_type", nullable = false)
    private String sourceType; // JOB_DESCRIPTION | COMPANY_DOC | PAST_INTERVIEW

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "title")
    private String title;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
