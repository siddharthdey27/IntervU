package com.preppilot.controller;

import com.preppilot.dto.AskQuestionRequest;
import com.preppilot.dto.ChatTurnResponse;
import com.preppilot.dto.StartSessionRequest;
import com.preppilot.entity.InterviewMessage;
import com.preppilot.entity.InterviewSession;
import com.preppilot.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/start")
    public ResponseEntity<InterviewSession> start(Authentication auth, @Valid @RequestBody StartSessionRequest req) {
        UUID userId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(interviewService.startSession(userId, req));
    }

    @PostMapping("/message")
    public ResponseEntity<ChatTurnResponse> sendMessage(Authentication auth, @Valid @RequestBody AskQuestionRequest req) {
        UUID userId = UUID.fromString(auth.getName());
        String reply = interviewService.continueSession(userId, UUID.fromString(req.sessionId()), req.userMessage());
        return ResponseEntity.ok(new ChatTurnResponse(req.sessionId(), reply));
    }

    @GetMapping("/{sessionId}/transcript")
    public ResponseEntity<List<InterviewMessage>> transcript(@PathVariable String sessionId) {
        return ResponseEntity.ok(interviewService.getTranscript(UUID.fromString(sessionId)));
    }
}
