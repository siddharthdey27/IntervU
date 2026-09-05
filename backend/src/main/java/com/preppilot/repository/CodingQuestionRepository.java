package com.preppilot.repository;

import com.preppilot.entity.CodingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodingQuestionRepository extends JpaRepository<CodingQuestion, Long> {
    Optional<CodingQuestion> findByTitle(String title);
}
