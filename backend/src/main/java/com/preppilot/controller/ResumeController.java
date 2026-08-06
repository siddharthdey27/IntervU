package com.preppilot.controller;

import com.preppilot.entity.Resume;
import com.preppilot.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<Resume> upload(Authentication auth, @RequestParam("file") MultipartFile file) throws IOException {
        UUID userId = UUID.fromString(auth.getName());
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(resumeService.ingestResume(userId, file));
    }

    @GetMapping
    public ResponseEntity<List<Resume>> list(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(resumeService.listForUser(userId));
    }
}
