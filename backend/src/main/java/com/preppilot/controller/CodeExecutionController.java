package com.preppilot.controller;

import com.preppilot.dto.CodeRunRequest;
import com.preppilot.dto.CodeRunResult;
import com.preppilot.dto.SubmissionResultDto;
import com.preppilot.service.CodeExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coding-questions/{questionId}")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;

    public CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    /** Ad-hoc run against a custom input, no grading/persistence. */
    @PostMapping("/run")
    public ResponseEntity<CodeRunResult> run(@PathVariable Long questionId, @RequestBody CodeRunRequest request) {
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
