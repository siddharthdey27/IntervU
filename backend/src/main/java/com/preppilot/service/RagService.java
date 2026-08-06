package com.preppilot.service;

import com.preppilot.service.impl.VectorStoreServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {

    private final EmbeddingService embeddingService;
    private final VectorStoreServiceImpl vectorStore;

    @Value("${app.rag.top-k}")
    private int topK;

    /**
     * Retrieves the most relevant resume chunks + (optionally) company/JD knowledge
     * chunks for a given user query, and formats them as context for the LLM prompt.
     */
    public String retrieveContext(UUID resumeId, UUID userId, String query, String companyName) {
        float[] queryEmbedding = embeddingService.embed(query);

        List<String> resumeChunks = resumeId != null
                ? vectorStore.findTopKChunksForResume(resumeId, queryEmbedding, topK)
                : vectorStore.findTopKChunksForUser(userId, queryEmbedding, topK);

        List<String> knowledgeChunks = vectorStore.findTopKKnowledgeChunks(companyName, queryEmbedding, topK);

        StringBuilder sb = new StringBuilder();
        if (!resumeChunks.isEmpty()) {
            sb.append("Relevant resume excerpts:\n")
              .append(resumeChunks.stream().collect(Collectors.joining("\n---\n")))
              .append("\n\n");
        }
        if (!knowledgeChunks.isEmpty()) {
            sb.append("Relevant company/job-description context:\n")
              .append(knowledgeChunks.stream().collect(Collectors.joining("\n---\n")));
        }
        return sb.toString();
    }
}
