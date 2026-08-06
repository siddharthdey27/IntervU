package com.preppilot.dto;

import jakarta.validation.constraints.NotBlank;

public record AskQuestionRequest(
        @NotBlank String sessionId,
        @NotBlank String userMessage
) {}
