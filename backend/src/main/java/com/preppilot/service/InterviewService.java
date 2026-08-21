package com.preppilot.service;

import com.preppilot.dto.StartSessionRequest;
import com.preppilot.entity.InterviewMessage;
import com.preppilot.entity.InterviewSession;
import com.preppilot.repository.InterviewMessageRepository;
import com.preppilot.repository.InterviewSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewMessageRepository messageRepository;
    private final RagService ragService;
    private final ChatModelService chatModelService;

    private static final String BASE_SYSTEM_PROMPT = """
            You are an expert technical interviewer conducting a %s interview%s.
            Ask one question at a time, wait for the candidate's answer, then give brief
            constructive feedback before asking the next question. Tailor questions to the
            candidate's background using the resume context provided below when relevant.
            Keep questions realistic and progressively adjust difficulty based on answer quality.

            Context:
            %s
            """;

    public InterviewSession startSession(UUID userId, StartSessionRequest req) {
        InterviewSession session = InterviewSession.builder()
                .userId(userId)
                .resumeId(req.resumeId() != null ? UUID.fromString(req.resumeId()) : null)
                .sessionType(req.sessionType())
                .topic(req.topic())
                .companyName(req.companyName())
                .build();
        session = sessionRepository.save(session);

        String firstQuestion = generateNextQuestion(session, userId, "Begin the interview with an introductory question.");

        messageRepository.save(InterviewMessage.builder()
                .sessionId(session.getId())
                .sender("AI")
                .content(firstQuestion)
                .build());

        return session;
    }

    public String continueSession(UUID userId, UUID sessionId, String userAnswer) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        ensureSessionOwner(session, userId);

        messageRepository.save(InterviewMessage.builder()
                .sessionId(sessionId)
                .sender("USER")
                .content(userAnswer)
                .build());

        String aiReply = generateNextQuestion(session, userId, userAnswer);

        messageRepository.save(InterviewMessage.builder()
                .sessionId(sessionId)
                .sender("AI")
                .content(aiReply)
                .build());

        return aiReply;
    }

    public List<InterviewMessage> getTranscript(UUID userId, UUID sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        ensureSessionOwner(session, userId);
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    private void ensureSessionOwner(InterviewSession session, UUID userId) {
        if (!session.getUserId().equals(userId)) {
            throw new SecurityException("You do not have access to this interview session");
        }
    }

    private String generateNextQuestion(InterviewSession session, UUID userId, String latestUserInput) {
        String companySuffix = session.getCompanyName() != null
                ? " for a position at " + session.getCompanyName()
                : "";
        String topicLabel = session.getTopic() != null ? session.getTopic() : session.getSessionType();

        String context = ragService.retrieveContext(
                session.getResumeId(), userId, latestUserInput, session.getCompanyName());

        String systemPrompt = BASE_SYSTEM_PROMPT.formatted(topicLabel, companySuffix, context);

        List<InterviewMessage> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        int maxTurns = 10;
        if (history.size() > maxTurns) {
            history = history.subList(history.size() - maxTurns, history.size());
        }
        List<String[]> conversation = history.stream()
                .map(m -> new String[]{m.getSender(), m.getContent()})
                .toList();

        return chatModelService.generateReply(systemPrompt, conversation);
    }
}
