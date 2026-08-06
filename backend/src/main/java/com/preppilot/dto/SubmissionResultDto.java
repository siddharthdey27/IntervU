package com.preppilot.dto;

import java.util.List;

public record SubmissionResultDto(
    Long submissionId,
    String status,            // ACCEPTED | WRONG_ANSWER | ERROR | TIMEOUT
    int passedTestCount,
    int totalTestCount,
    List<TestCaseResult> visibleResults  // results for non-hidden test cases only
) {
    public record TestCaseResult(
        String input,
        String expectedOutput,
        String actualOutput,
        boolean passed
    ) {}
}
