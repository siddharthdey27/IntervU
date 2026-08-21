package com.preppilot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingServiceTest {

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService();
    }

    @Test
    void embed_geminiFallbackWhenKeyMissingOrDummy() {
        ReflectionTestUtils.setField(embeddingService, "provider", "gemini");
        ReflectionTestUtils.setField(embeddingService, "geminiApiKey", "your_gemini_api_key_here");
        ReflectionTestUtils.setField(embeddingService, "embeddingModelName", "text-embedding-004");

        float[] vector = embeddingService.embed("Test sentence for embedding");

        assertNotNull(vector);
        assertEquals(768, vector.length);

        // Verify fallback vector is unit normalized
        float sumSq = 0f;
        for (float v : vector) {
            sumSq += v * v;
        }
        assertTrue(Math.abs(1.0f - sumSq) < 1e-4);
    }

    @Test
    void embed_geminiDeterministicFallbackForSameInput() {
        ReflectionTestUtils.setField(embeddingService, "provider", "gemini");
        ReflectionTestUtils.setField(embeddingService, "geminiApiKey", "");

        float[] vector1 = embeddingService.embed("Deterministic test");
        float[] vector2 = embeddingService.embed("Deterministic test");

        assertArrayEquals(vector1, vector2);
    }

    @Test
    void embed_openaiFallbackWhenModelFailsOrNull() {
        ReflectionTestUtils.setField(embeddingService, "provider", "openai");
        ReflectionTestUtils.setField(embeddingService, "openAiApiKey", "");

        float[] vector = embeddingService.embed("OpenAI fallback test");

        assertNotNull(vector);
        assertEquals(768, vector.length);
    }
}
