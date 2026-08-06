package com.preppilot.dto;

import jakarta.validation.constraints.NotBlank;

public record StartSessionRequest(
        String resumeId,
        @NotBlank String sessionType,   // TEXT | VOICE | CODING | SYSTEM_DESIGN
        String topic,                   // e.g. "Java", "System Design"
        String companyName              // optional, enables company-specific RAG
) {}
