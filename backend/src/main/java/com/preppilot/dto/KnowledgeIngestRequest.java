package com.preppilot.dto;

import jakarta.validation.constraints.NotBlank;

public record KnowledgeIngestRequest(
        @NotBlank String sourceType, // JOB_DESCRIPTION | COMPANY_DOC | PAST_INTERVIEW
        String companyName,
        String title,
        @NotBlank String content
) {}
