package com.preppilot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class Judge0ServiceTest {

    private Judge0Service judge0Service;
    private MockRestServiceServer mockServer;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        judge0Service = new Judge0Service();
        ReflectionTestUtils.setField(judge0Service, "judge0BaseUrl", "http://judge0-test:2358");

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(judge0Service, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void execute_successfulExecution() throws Exception {
        Map<String, Object> responseMap = Map.of(
                "stdout", "Hello World\n",
                "stderr", "",
                "compile_output", "",
                "status", Map.of("description", "Accepted"),
                "time", 0.045,
                "memory", 1200
        );

        mockServer.expect(requestTo("http://judge0-test:2358/submissions?base64_encoded=false&wait=true"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(mapper.writeValueAsString(responseMap), MediaType.APPLICATION_JSON));

        Judge0Service.Judge0Result result = judge0Service.execute("python", "print('Hello World')", "", 2000, 128000);

        mockServer.verify();
        assertNotNull(result);
        assertEquals("Hello World\n", result.stdout());
        assertEquals("Accepted", result.statusDescription());
        assertEquals(45, result.timeMs());
        assertEquals(1200, result.memoryKb());
    }

    @Test
    void execute_unsupportedLanguage_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                judge0Service.execute("rust", "fn main() {}", "", 2000, 128000)
        );
    }

    @Test
    void execute_compilationError_parsedCorrectly() throws Exception {
        Map<String, Object> responseMap = Map.of(
                "status", Map.of("description", "Compilation Error"),
                "compile_output", "error: ';' expected"
        );

        mockServer.expect(requestTo("http://judge0-test:2358/submissions?base64_encoded=false&wait=true"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(mapper.writeValueAsString(responseMap), MediaType.APPLICATION_JSON));

        Judge0Service.Judge0Result result = judge0Service.execute("java", "class Main {", "", 2000, 128000);

        mockServer.verify();
        assertNotNull(result);
        assertEquals("Compilation Error", result.statusDescription());
        assertEquals("error: ';' expected", result.compileOutput());
        assertNull(result.stdout());
    }

    @Test
    void execute_timeoutOrServerError_throwsException() {
        mockServer.expect(requestTo("http://judge0-test:2358/submissions?base64_encoded=false&wait=true"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThrows(Exception.class, () ->
                judge0Service.execute("python", "while True: pass", "", 2000, 128000)
        );
        mockServer.verify();
    }
}
