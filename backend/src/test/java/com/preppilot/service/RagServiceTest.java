package com.preppilot.service;

import com.preppilot.service.impl.VectorStoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RagServiceTest {

    private EmbeddingService embeddingService;
    private VectorStoreServiceImpl vectorStore;
    private RagService ragService;

    @BeforeEach
    void setUp() {
        embeddingService = mock(EmbeddingService.class);
        vectorStore = mock(VectorStoreServiceImpl.class);
        ragService = new RagService(embeddingService, vectorStore);
        ReflectionTestUtils.setField(ragService, "topK", 3);
    }

    @Test
    void retrieveContext_withResumeIdAndKnowledge_formatsBothContexts() {
        UUID resumeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String query = "system design experience";
        String companyName = "Acme Corp";
        float[] fakeEmbedding = new float[]{0.1f, 0.2f, 0.3f};

        when(embeddingService.embed(query)).thenReturn(fakeEmbedding);
        when(vectorStore.findTopKChunksForResume(resumeId, fakeEmbedding, 3))
                .thenReturn(List.of("Resume Chunk 1: Built distributed systems", "Resume Chunk 2: Java & Spring"));
        when(vectorStore.findTopKKnowledgeChunks(companyName, fakeEmbedding, 3))
                .thenReturn(List.of("JD Chunk 1: Senior backend engineer requirements"));

        String context = ragService.retrieveContext(resumeId, userId, query, companyName);

        assertTrue(context.contains("Relevant resume excerpts:"));
        assertTrue(context.contains("Resume Chunk 1: Built distributed systems"));
        assertTrue(context.contains("Relevant company/job-description context:"));
        assertTrue(context.contains("JD Chunk 1: Senior backend engineer requirements"));
        verify(vectorStore).findTopKChunksForResume(resumeId, fakeEmbedding, 3);
        verify(vectorStore, never()).findTopKChunksForUser(any(), any(), anyInt());
    }

    @Test
    void retrieveContext_withoutResumeId_fallsBackToUserId() {
        UUID userId = UUID.randomUUID();
        String query = "explain microservices";
        float[] fakeEmbedding = new float[]{0.5f, 0.5f};

        when(embeddingService.embed(query)).thenReturn(fakeEmbedding);
        when(vectorStore.findTopKChunksForUser(userId, fakeEmbedding, 3))
                .thenReturn(List.of("User Chunk 1"));
        when(vectorStore.findTopKKnowledgeChunks(null, fakeEmbedding, 3))
                .thenReturn(Collections.emptyList());

        String context = ragService.retrieveContext(null, userId, query, null);

        assertTrue(context.contains("Relevant resume excerpts:"));
        assertTrue(context.contains("User Chunk 1"));
        verify(vectorStore).findTopKChunksForUser(userId, fakeEmbedding, 3);
        verify(vectorStore, never()).findTopKChunksForResume(any(), any(), anyInt());
    }

    @Test
    void retrieveContext_emptyResults_returnsEmptyString() {
        UUID userId = UUID.randomUUID();
        String query = "no match query";
        float[] fakeEmbedding = new float[]{0.0f, 0.0f};

        when(embeddingService.embed(query)).thenReturn(fakeEmbedding);
        when(vectorStore.findTopKChunksForUser(userId, fakeEmbedding, 3)).thenReturn(Collections.emptyList());
        when(vectorStore.findTopKKnowledgeChunks(null, fakeEmbedding, 3)).thenReturn(Collections.emptyList());

        String context = ragService.retrieveContext(null, userId, query, null);

        assertEquals("", context);
    }
}
