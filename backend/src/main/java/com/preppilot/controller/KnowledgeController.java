package com.preppilot.controller;

import com.preppilot.dto.KnowledgeIngestRequest;
import com.preppilot.entity.KnowledgeDocument;
import com.preppilot.service.KnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/ingest")
    public ResponseEntity<KnowledgeDocument> ingestDocument(@Valid @RequestBody KnowledgeIngestRequest req) {
        KnowledgeDocument doc = knowledgeService.ingestDocument(req);
        return ResponseEntity.ok(doc);
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeDocument>> listDocuments(@RequestParam(required = false) String companyName) {
        if (companyName != null && !companyName.isBlank()) {
            return ResponseEntity.ok(knowledgeService.listByCompany(companyName));
        }
        return ResponseEntity.ok(knowledgeService.listAll());
    }
}
