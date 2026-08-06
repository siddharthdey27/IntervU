package com.preppilot.controller;

import com.preppilot.dto.ProgressDto;
import com.preppilot.entity.CodeSubmission;
import com.preppilot.entity.CodingQuestion;
import com.preppilot.entity.InterviewSession;
import com.preppilot.repository.CodeSubmissionRepository;
import com.preppilot.repository.CodingQuestionRepository;
import com.preppilot.repository.InterviewSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final InterviewSessionRepository sessionRepository;
    private final CodeSubmissionRepository submissionRepository;
    private final CodingQuestionRepository questionRepository;

    @GetMapping
    public ResponseEntity<ProgressDto> getProgress(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        String userIdStr = auth.getName();

        // Session stats
        List<InterviewSession> sessions = sessionRepository.findByUserIdOrderByStartedAtDesc(userId);
        int totalSessions = sessions.size();
        int completedSessions = (int) sessions.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .count();

        // Coding submission stats
        List<CodeSubmission> submissions = submissionRepository.findByUserIdOrderByCreatedAtDesc(userIdStr);
        int totalCodingSubmissions = submissions.size();
        int acceptedSubmissions = (int) submissions.stream()
                .filter(s -> "ACCEPTED".equals(s.getStatus()))
                .count();

        // Recent sessions (last 10)
        List<ProgressDto.SessionSummary> recentSessions = sessions.stream()
                .limit(10)
                .map(s -> new ProgressDto.SessionSummary(
                        s.getId().toString(),
                        s.getSessionType(),
                        s.getTopic(),
                        s.getCompanyName(),
                        s.getStatus(),
                        s.getStartedAt()
                ))
                .toList();

        // Recent submissions (last 10) with question titles
        List<ProgressDto.SubmissionSummary> recentSubmissions = submissions.stream()
                .limit(10)
                .map(sub -> {
                    String title = questionRepository.findById(sub.getQuestionId())
                            .map(CodingQuestion::getTitle)
                            .orElse("Unknown");
                    return new ProgressDto.SubmissionSummary(
                            sub.getId(),
                            sub.getQuestionId(),
                            title,
                            sub.getLanguage(),
                            sub.getStatus(),
                            sub.getPassedTestCount(),
                            sub.getTotalTestCount()
                    );
                })
                .toList();

        return ResponseEntity.ok(new ProgressDto(
                totalSessions, completedSessions,
                totalCodingSubmissions, acceptedSubmissions,
                recentSessions, recentSubmissions
        ));
    }
}
