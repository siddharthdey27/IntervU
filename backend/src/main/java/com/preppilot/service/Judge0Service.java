package com.preppilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Code execution service that supports two backends:
 * 
 * 1. **Piston** (default) — free public API at https://emkc.org/api/v2/piston
 *    No API key, no Docker, works out of the box.
 *    Set CODE_EXEC_PROVIDER=piston (or leave blank, it's the default).
 *
 * 2. **Judge0** — self-hosted via Docker (see docker-compose.judge0.yml).
 *    Set CODE_EXEC_PROVIDER=judge0 and JUDGE0_BASE_URL accordingly.
 */
@Service
public class Judge0Service {

    private static final Logger log = LoggerFactory.getLogger(Judge0Service.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${code-exec.provider:piston}")
    private String provider;

    @Value("${judge0.base-url:http://judge0-server:2358}")
    private String judge0BaseUrl;

    @Value("${piston.base-url:https://emkc.org/api/v2/piston}")
    private String pistonBaseUrl;

    @Value("${wandbox.base-url:https://wandbox.org/api}")
    private String wandboxBaseUrl;

    // Judge0 CE language IDs
    private static final Map<String, Integer> JUDGE0_LANGUAGE_IDS = Map.of(
        "java", 62,
        "python", 71,
        "javascript", 63,
        "cpp", 54
    );

    // Piston language identifiers + versions
    private static final Map<String, String[]> PISTON_LANGUAGES = Map.of(
        "java",       new String[]{"java", "15.0.2"},
        "python",     new String[]{"python", "3.10.0"},
        "javascript", new String[]{"javascript", "18.15.0"},
        "cpp",        new String[]{"c++", "10.2.0"}
    );

    // Wandbox compiler identifiers
    private static final Map<String, String> WANDBOX_COMPILERS = Map.of(
        "java",       "openjdk-jdk-21+35",
        "python",     "cpython-3.10.15",
        "javascript", "nodejs-20.17.0",
        "cpp",        "gcc-13.2.0"
    );

    public record Judge0Result(String stdout, String stderr, String compileOutput,
                                String statusDescription, Integer timeMs, Integer memoryKb) {}

    public Judge0Result execute(String language, String sourceCode, String stdin,
                                int timeLimitMs, int memoryLimitKb) {
        if ("judge0".equalsIgnoreCase(provider)) {
            return executeWithJudge0(language, sourceCode, stdin, timeLimitMs, memoryLimitKb);
        } else if ("piston".equalsIgnoreCase(provider)) {
            return executeWithPiston(language, sourceCode, stdin);
        }
        return executeWithWandbox(language, sourceCode, stdin);
    }

    // ── Wandbox implementation ───────────────────────────────────────

    private Judge0Result executeWithWandbox(String language, String sourceCode, String stdin) {
        String compiler = WANDBOX_COMPILERS.get(language.toLowerCase());
        if (compiler == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        // For Java in Wandbox, replace "public class" with "class" to avoid "class X is public, should be declared in file X.java"
        String preparedCode = sourceCode;
        if ("java".equalsIgnoreCase(language)) {
            preparedCode = preparedCode.replaceAll("\\bpublic\\s+class\\b", "class");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("compiler", compiler);
        body.put("code", preparedCode);
        if (stdin != null && !stdin.isEmpty()) {
            body.put("stdin", stdin);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "PrepPilot-App/1.0");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = wandboxBaseUrl + "/compile.json";
        log.debug("Wandbox request: POST {} compiler={}", url, compiler);

        String rawResponse;
        try {
            rawResponse = restTemplate.postForObject(url, entity, String.class);
        } catch (Exception e) {
            log.error("Wandbox API call failed: {}", e.getMessage());
            return new Judge0Result(null, "Code execution service unavailable: " + e.getMessage(),
                null, "Error", null, null);
        }

        if (rawResponse == null || rawResponse.isBlank()) {
            return new Judge0Result(null, "No response from code execution service",
                null, "Error", null, null);
        }

        JsonNode response;
        try {
            response = mapper.readTree(rawResponse);
        } catch (Exception e) {
            log.error("Failed to parse Wandbox response: {}", e.getMessage());
            return new Judge0Result(null, "Failed to parse code execution response: " + e.getMessage(),
                null, "Error", null, null);
        }

        String compilerError = textOrNull(response, "compiler_error");
        if (compilerError == null || compilerError.isBlank()) {
            compilerError = textOrNull(response, "compiler_message");
        }

        int exitStatus = response.path("status").asInt(0);
        if (exitStatus != 0 && compilerError != null && !compilerError.isBlank()) {
            return new Judge0Result(null, null, compilerError, "Compilation Error", null, null);
        }

        String progOut = textOrNull(response, "program_output");
        String progErr = textOrNull(response, "program_error");

        String statusDescription;
        if (exitStatus == 0) {
            statusDescription = "Accepted";
        } else {
            statusDescription = "Runtime Error (exit code " + exitStatus + ")";
        }

        return new Judge0Result(progOut, progErr, compilerError, statusDescription, null, null);
    }

    // ── Piston implementation ───────────────────────────────────────

    private Judge0Result executeWithPiston(String language, String sourceCode, String stdin) {
        String[] langInfo = PISTON_LANGUAGES.get(language.toLowerCase());
        if (langInfo == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        Map<String, Object> file = new HashMap<>();
        file.put("name", getFileName(language));
        file.put("content", sourceCode);

        Map<String, Object> body = new HashMap<>();
        body.put("language", langInfo[0]);
        body.put("version", langInfo[1]);
        body.put("files", List.of(file));
        if (stdin != null && !stdin.isEmpty()) {
            body.put("stdin", stdin);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = pistonBaseUrl + "/execute";
        log.debug("Piston request: POST {} lang={}", url, langInfo[0]);

        JsonNode response;
        try {
            response = restTemplate.postForObject(url, entity, JsonNode.class);
        } catch (Exception e) {
            log.error("Piston API call failed: {}", e.getMessage());
            return new Judge0Result(null, "Code execution service unavailable: " + e.getMessage(),
                null, "Error", null, null);
        }

        if (response == null) {
            return new Judge0Result(null, "No response from code execution service",
                null, "Error", null, null);
        }

        // Piston response structure:
        // { "run": { "stdout": "...", "stderr": "...", "code": 0, "output": "..." },
        //   "compile": { "stdout": "...", "stderr": "...", "code": 0, "output": "..." } (for compiled langs) }

        String compileStderr = null;
        JsonNode compileNode = response.get("compile");
        if (compileNode != null && !compileNode.isNull()) {
            int compileCode = compileNode.path("code").asInt(0);
            if (compileCode != 0) {
                compileStderr = textOrNull(compileNode, "stderr");
                if (compileStderr == null || compileStderr.isBlank()) {
                    compileStderr = textOrNull(compileNode, "output");
                }
                return new Judge0Result(null, null, compileStderr, "Compilation Error", null, null);
            }
        }

        JsonNode runNode = response.get("run");
        if (runNode == null || runNode.isNull()) {
            return new Judge0Result(null, "No run output from execution service",
                null, "Error", null, null);
        }

        String stdout = textOrNull(runNode, "stdout");
        String stderr = textOrNull(runNode, "stderr");
        int exitCode = runNode.path("code").asInt(0);

        String statusDescription;
        if (exitCode == 0 && (stderr == null || stderr.isBlank())) {
            statusDescription = "Accepted";
        } else if (exitCode != 0) {
            statusDescription = "Runtime Error (exit code " + exitCode + ")";
        } else {
            statusDescription = "Accepted";
        }

        return new Judge0Result(stdout, stderr, null, statusDescription, null, null);
    }

    private String getFileName(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> "Main.java";
            case "python" -> "main.py";
            case "javascript" -> "main.js";
            case "cpp" -> "main.cpp";
            default -> "code";
        };
    }

    // ── Judge0 implementation (original) ────────────────────────────

    private Judge0Result executeWithJudge0(String language, String sourceCode, String stdin,
                                            int timeLimitMs, int memoryLimitKb) {
        Integer languageId = JUDGE0_LANGUAGE_IDS.get(language.toLowerCase());
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

    // ── helpers ─────────────────────────────────────────────────────

    private String textOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }
}
