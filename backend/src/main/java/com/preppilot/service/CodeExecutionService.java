package com.preppilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.preppilot.dto.CodeRunRequest;
import com.preppilot.dto.CodeRunResult;
import com.preppilot.dto.SubmissionResultDto;
import com.preppilot.entity.CodeSubmission;
import com.preppilot.entity.CodingQuestion;
import com.preppilot.repository.CodeSubmissionRepository;
import com.preppilot.repository.CodingQuestionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CodeExecutionService {

    private final Judge0Service judge0Service;
    private final CodingQuestionRepository questionRepository;
    private final CodeSubmissionRepository submissionRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public CodeExecutionService(Judge0Service judge0Service,
                                 CodingQuestionRepository questionRepository,
                                 CodeSubmissionRepository submissionRepository) {
        this.judge0Service = judge0Service;
        this.questionRepository = questionRepository;
        this.submissionRepository = submissionRepository;
    }

    /** Ad-hoc "Run" against a single custom (or sample) input — no grading, no persistence. */
    public CodeRunResult runAdHoc(Long questionId, CodeRunRequest request) {
        CodingQuestion question = questionRepository.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));

        var result = judge0Service.execute(
            request.language(), request.sourceCode(), request.stdin(),
            question.getTimeLimitMs(), question.getMemoryLimitKb()
        );

        String status = result.compileOutput() != null && !result.compileOutput().isBlank()
            ? "Compilation Error"
            : result.statusDescription();

        String err = (result.stderr() != null && !result.stderr().isBlank())
            ? result.stderr()
            : result.compileOutput();

        return new CodeRunResult(result.stdout(), err,
            status, result.timeMs(), result.memoryKb());
    }

    /** "Submit" — runs against every test case, grades, and persists a CodeSubmission. */
    public SubmissionResultDto submit(String userId, Long questionId, CodeRunRequest request) throws Exception {
        CodingQuestion question = questionRepository.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));

        JsonNode testCases = mapper.readTree(question.getTestCases());
        int passed = 0;
        int total = testCases.size();
        List<SubmissionResultDto.TestCaseResult> visibleResults = new ArrayList<>();
        String lastStderr = null;
        Integer lastTimeMs = null;
        Integer lastMemoryKb = null;
        boolean hadError = false;

        for (JsonNode tc : testCases) {
            String input = tc.path("input").asText("");
            String expected = tc.path("expected_output").asText("").trim();
            boolean hidden = tc.path("hidden").asBoolean(false);

            var result = judge0Service.execute(
                request.language(), request.sourceCode(), input,
                question.getTimeLimitMs(), question.getMemoryLimitKb()
            );

            String actual = result.stdout() != null ? result.stdout().trim() : "";
            boolean testPassed = actual.equals(expected)
                && (result.stderr() == null || result.stderr().isBlank())
                && (result.compileOutput() == null || result.compileOutput().isBlank());

            if (testPassed) passed++;
            String tcErr = (result.stderr() != null && !result.stderr().isBlank())
                ? result.stderr()
                : result.compileOutput();
            if (tcErr != null && !tcErr.isBlank()) hadError = true;
            lastStderr = tcErr;
            lastTimeMs = result.timeMs();
            lastMemoryKb = result.memoryKb();

            if (!hidden) {
                visibleResults.add(new SubmissionResultDto.TestCaseResult(input, expected, actual, testPassed));
            }
        }

        String status;
        if (hadError) status = "ERROR";
        else if (passed == total) status = "ACCEPTED";
        else status = "WRONG_ANSWER";

        CodeSubmission submission = new CodeSubmission();
        submission.setUserId(userId);
        submission.setQuestionId(questionId);
        submission.setLanguage(request.language());
        submission.setSourceCode(request.sourceCode());
        submission.setStatus(status);
        submission.setPassedTestCount(passed);
        submission.setTotalTestCount(total);
        submission.setStderr(lastStderr);
        submission.setExecutionTimeMs(lastTimeMs);
        submission.setMemoryKb(lastMemoryKb);
        submission = submissionRepository.save(submission);

        return new SubmissionResultDto(submission.getId(), status, passed, total, visibleResults);
    }
}
