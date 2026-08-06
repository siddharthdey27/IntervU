package com.preppilot.controller;

import com.preppilot.entity.CodingQuestion;
import com.preppilot.repository.CodingQuestionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coding-questions")
public class CodingQuestionController {

    private final CodingQuestionRepository questionRepository;

    public CodingQuestionController(CodingQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping
    public List<CodingQuestion> list() {
        return questionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodingQuestion> get(@PathVariable Long id) {
        return questionRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
