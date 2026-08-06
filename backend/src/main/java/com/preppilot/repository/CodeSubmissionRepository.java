package com.preppilot.repository;

import com.preppilot.entity.CodeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {
    List<CodeSubmission> findByUserIdAndQuestionIdOrderByCreatedAtDesc(String userId, Long questionId);
    List<CodeSubmission> findByUserIdOrderByCreatedAtDesc(String userId);
    long countByUserId(String userId);
    long countByUserIdAndStatus(String userId, String status);
}
