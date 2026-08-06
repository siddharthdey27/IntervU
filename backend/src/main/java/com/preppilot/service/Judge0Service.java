package com.preppilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Talks to a self-hosted Judge0 CE instance (see docker-compose.yml "judge0-server" service).
 * No API key required - this is the free, open-source, self-hosted version of Judge0,
 * NOT the paid RapidAPI-hosted one.
 */
@Service
public class Judge0Service {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${judge0.base-url:http://judge0-server:2358}")
    private String judge0BaseUrl;

    // Judge0 language IDs (CE package) — see https://ce.judge0.com/languages
    private static final Map<String, Integer> LANGUAGE_IDS = Map.of(
        "java", 62,        // Java (OpenJDK 17.0.6)
        "python", 71,      // Python (3.8.1)
        "javascript", 63   // JavaScript (Node.js 12.14.0)
    );

    public record Judge0Result(String stdout, String stderr, String compileOutput,
                                String statusDescription, Integer timeMs, Integer memoryKb) {}

    public Judge0Result execute(String language, String sourceCode, String stdin, int timeLimitMs, int memoryLimitKb) {
        Integer languageId = LANGUAGE_IDS.get(language.toLowerCase());
        if (languageId == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        Map<String, Object> body = Map.of(
            "language_id", languageId,
            "source_code", sourceCode,
            "stdin", stdin == null ? "" : stdin,
            "cpu_time_limit", timeLimitMs / 1000.0,
            "memory_limit", memoryLimitKb
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // wait=true -> synchronous submission, base64_encoded=false -> plain text I/O
        String url = judge0BaseUrl + "/submissions?base64_encoded=false&wait=true";
        JsonNode response = restTemplate.postForObject(url, entity, JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("No response from Judge0");
        }

        String stdout = textOrNull(response, "stdout");
        String stderr = textOrNull(response, "stderr");
        String compileOutput = textOrNull(response, "compile_output");
        String statusDescription = response.path("status").path("description").asText("Unknown");
        Integer timeMs = response.hasNonNull("time")
            ? (int) Math.round(response.get("time").asDouble() * 1000)
            : null;
        Integer memoryKb = response.hasNonNull("memory") ? response.get("memory").asInt() : null;

        return new Judge0Result(stdout, stderr, compileOutput, statusDescription, timeMs, memoryKb);
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }
}
