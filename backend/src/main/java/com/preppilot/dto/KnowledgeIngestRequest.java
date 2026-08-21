package com.preppilot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record KnowledgeIngestRequest(
        @NotBlank
        @Pattern(regexp = "^(JOB_DESCRIPTION|COMPANY_DOC|PAST_INTERVIEW)$", message = "sourceType must be JOB_DESCRIPTION, COMPANY_DOC, or PAST_INTERVIEW")
        String sourceType,

        @Size(max = 200, message = "companyName cannot exceed 200 characters")
        String companyName,

        @Size(max = 200, message = "title cannot exceed 200 characters")
        String title,

        @NotBlank
        @Size(min = 1, max = 50000, message = "Content must be between 1 and 50,000 characters")
        String content
) {}
