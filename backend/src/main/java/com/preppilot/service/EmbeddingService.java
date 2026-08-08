package com.preppilot.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    @Value("${app.ai.provider:gemini}")
    private String provider;

    @Value("${app.ai.openai-api-key:}")
    private String openAiApiKey;

    @Value("${app.ai.gemini-api-key:}")
    private String geminiApiKey;

    @Value("${app.ai.embedding-model:text-embedding-004}")
    private String embeddingModelName;

    private EmbeddingModel embeddingModel;

    @PostConstruct
    void init() {
        if ("gemini".equalsIgnoreCase(provider)) {
            this.embeddingModel = GoogleAiGeminiEmbeddingModel.builder()
                    .apiKey(geminiApiKey)
                    .modelName(embeddingModelName)
                    .build();
        } else {
            this.embeddingModel = OpenAiEmbeddingModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(embeddingModelName)
                    .build();
        }
    }

    /** Returns the embedding vector as a float[] for a given piece of text. */
    public float[] embed(String text) {
        Embedding embedding = embeddingModel.embed(TextSegment.from(text)).content();
        return embedding.vector();
    }
}
