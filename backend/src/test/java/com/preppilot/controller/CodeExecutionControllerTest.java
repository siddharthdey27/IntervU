package com.preppilot.controller;

import com.preppilot.dto.CodeRunRequest;
import com.preppilot.dto.CodeRunResult;
import com.preppilot.dto.SubmissionResultDto;
import com.preppilot.service.CodeExecutionRateLimiter;
import com.preppilot.service.CodeExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CodeExecutionControllerTest {

    private CodeExecutionService codeExecutionService;
    private CodeExecutionRateLimiter rateLimiter;
    private Authentication authentication;
    private CodeExecutionController controller;

    @BeforeEach
    void setUp() {
        codeExecutionService = mock(CodeExecutionService.class);
        rateLimiter = mock(CodeExecutionRateLimiter.class);
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user-123");
        controller = new CodeExecutionController(codeExecutionService, rateLimiter);
    }

    @Test
    void run_allowedWhenUnderRateLimit() {
        CodeRunRequest request = new CodeRunRequest("python", "print('hello')", "test-input");
        CodeRunResult expectedResult = new CodeRunResult("hello\n", null, "Accepted", 50, 1024);

        when(rateLimiter.tryAcquire("user-123")).thenReturn(true);
        when(codeExecutionService.runAdHoc(1L, request)).thenReturn(expectedResult);

        ResponseEntity<CodeRunResult> response = controller.run(1L, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
        verify(codeExecutionService).runAdHoc(1L, request);
    }

    @Test
    void run_throwsTooManyRequestsWhenRateLimitExceeded() {
        CodeRunRequest request = new CodeRunRequest("python", "print('hello')", "test-input");
        when(rateLimiter.tryAcquire("user-123")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.run(1L, request, authentication)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
        verifyNoInteractions(codeExecutionService);
    }

    @Test
    void submit_allowedWhenUnderRateLimit() throws Exception {
        CodeRunRequest request = new CodeRunRequest("python", "print('hello')", null);
        SubmissionResultDto expectedResult = new SubmissionResultDto(
                99L, "ACCEPTED", 2, 2, List.of()
        );

        when(rateLimiter.tryAcquire("user-123")).thenReturn(true);
        when(codeExecutionService.submit("user-123", 1L, request)).thenReturn(expectedResult);

        ResponseEntity<SubmissionResultDto> response = controller.submit(1L, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
        verify(codeExecutionService).submit("user-123", 1L, request);
    }

    @Test
    void submit_throwsTooManyRequestsWhenRateLimitExceeded() {
        CodeRunRequest request = new CodeRunRequest("python", "print('hello')", null);
        when(rateLimiter.tryAcquire("user-123")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                controller.submit(1L, request, authentication)
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
        verifyNoInteractions(codeExecutionService);
    }
}
