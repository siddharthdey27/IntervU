package com.preppilot.repository;

import com.preppilot.entity.InterviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterviewMessageRepository extends JpaRepository<InterviewMessage, UUID> {
    List<InterviewMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
