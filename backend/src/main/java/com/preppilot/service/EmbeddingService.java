package com.preppilot.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

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

    private EmbeddingModel openAiEmbeddingModel;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    void init() {
        if ("openai".equalsIgnoreCase(provider)) {
            this.openAiEmbeddingModel = OpenAiEmbeddingModel.builder()
                    .apiKey(openAiApiKey)
                    .modelName(embeddingModelName)
                    .build();
        }
    }

    /** Returns the embedding vector as a float[] for a given piece of text. */
    public float[] embed(String text) {
        if ("gemini".equalsIgnoreCase(provider)) {
            return embedGemini(text);
        } else {
            try {
                if (openAiEmbeddingModel != null) {
                    Embedding embedding = openAiEmbeddingModel.embed(TextSegment.from(text)).content();
                    return embedding.vector();
                }
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(EmbeddingService.class)
                        .warn("OpenAI embedding failed: {}. Using fallback vector.", e.getMessage());
            }
            return fallbackEmbedding(text);
        }
    }

    private float[] embedGemini(String text) {
        if (geminiApiKey == null || geminiApiKey.isBlank() || geminiApiKey.startsWith("your_")) {
            org.slf4j.LoggerFactory.getLogger(EmbeddingService.class)
                    .info("Gemini API key not configured. Using fallback embedding vector.");
            return fallbackEmbedding(text);
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" 
                + embeddingModelName + ":embedContent?key=" + geminiApiKey;

        Map<String, Object> body = Map.of(
                "model", "models/" + embeddingModelName,
                "content", Map.of("parts", List.of(Map.of("text", text)))
        );

        try {
            Map<?, ?> response = restTemplate.postForObject(url, body, Map.class);
            if (response != null && response.containsKey("embedding")) {
                Map<?, ?> embeddingObj = (Map<?, ?>) response.get("embedding");
                List<?> values = (List<?>) embeddingObj.get("values");
                float[] vector = new float[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    vector[i] = ((Number) values.get(i)).floatValue();
                }
                return vector;
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(EmbeddingService.class)
                    .warn("Failed to generate Gemini embedding: {}. Using fallback vector.", e.getMessage());
            return fallbackEmbedding(text);
        }
        return fallbackEmbedding(text);
    }

    private float[] fallbackEmbedding(String text) {
        int dim = 768;
        float[] vector = new float[dim];
        int hash = text.hashCode();
        java.util.Random rnd = new java.util.Random(hash);
        float sumSq = 0;
        for (int i = 0; i < dim; i++) {
            vector[i] = (float) (rnd.nextGaussian());
            sumSq += vector[i] * vector[i];
        }
        float norm = (float) Math.sqrt(sumSq);
        if (norm > 0) {
            for (int i = 0; i < dim; i++) {
                vector[i] /= norm;
            }
        }
        return vector;
    }
}
