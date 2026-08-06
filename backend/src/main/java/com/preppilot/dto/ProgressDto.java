package com.preppilot.dto;

import java.time.Instant;
import java.util.List;

public record ProgressDto(
    int totalSessions,
    int completedSessions,
    int totalCodingSubmissions,
    int acceptedSubmissions,
    List<SessionSummary> recentSessions,
    List<SubmissionSummary> recentSubmissions
) {
    public record SessionSummary(
        String id,
        String sessionType,
        String topic,
        String companyName,
        String status,
        Instant startedAt
    ) {}

    public record SubmissionSummary(
        Long id,
        Long questionId,
        String questionTitle,
        String language,
        String status,
        int passedTestCount,
        int totalTestCount
    ) {}
}
