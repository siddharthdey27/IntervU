package com.preppilot.dto;

// Used for both "Run" (custom/sample input) and "Submit" (runs against all test cases)
public record CodeRunRequest(
    String language,      // "java" | "python" | "javascript"
    String sourceCode,
    String stdin           // only used for ad-hoc "Run"; ignored for "Submit"
) {}
