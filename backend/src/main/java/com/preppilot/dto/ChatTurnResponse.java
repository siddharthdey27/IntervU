package com.preppilot.dto;

public record ChatTurnResponse(
        String sessionId,
        String aiMessage
) {}
