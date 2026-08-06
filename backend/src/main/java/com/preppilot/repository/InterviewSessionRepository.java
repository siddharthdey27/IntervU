package com.preppilot.repository;

import com.preppilot.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, UUID> {
    List<InterviewSession> findByUserIdOrderByStartedAtDesc(UUID userId);
    long countByUserId(UUID userId);
    long countByUserIdAndStatus(UUID userId, String status);
}
