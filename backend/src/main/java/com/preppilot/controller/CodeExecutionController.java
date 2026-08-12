package com.preppilot.controller;

import com.preppilot.dto.CodeRunRequest;
import com.preppilot.dto.CodeRunResult;
import com.preppilot.dto.SubmissionResultDto;
import com.preppilot.service.CodeExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coding-questions/{questionId}")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;
    private final CodeExecutionRateLimiter codeExecutionRateLimiter;

    public CodeExecutionController(CodeExecutionService codeExecutionService,
                                   CodeExecutionRateLimiter codeExecutionRateLimiter) {
        this.codeExecutionService = codeExecutionService;
        this.codeExecutionRateLimiter = codeExecutionRateLimiter;
    }

    /** Ad-hoc run against a custom input, no grading/persistence. */
    @PostMapping("/run")
    public ResponseEntity<CodeRunResult> run(@PathVariable Long questionId, @RequestBody CodeRunRequest request,
                                             Authentication authentication) {
        if (!codeExecutionRateLimiter.tryAcquire(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Code execution limit exceeded; try again later");
        }
        return ResponseEntity.ok(codeExecutionService.runAdHoc(questionId, request));
    }

    /** Full submission - runs all test cases and persists the result. */
    @PostMapping("/submit")
    public ResponseEntity<SubmissionResultDto> submit(@PathVariable Long questionId,
                                                        @RequestBody CodeRunRequest request,
                                                        Authentication authentication) throws Exception {
        String userId = authentication.getName();
        return ResponseEntity.ok(codeExecutionService.submit(userId, questionId, request));
    }
}
