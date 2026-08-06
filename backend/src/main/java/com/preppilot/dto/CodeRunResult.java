package com.preppilot.dto;

public record CodeRunResult(
    String stdout,
    String stderr,
    String status,        // e.g. "Accepted", "Wrong Answer", "Compilation Error", "Runtime Error", "Time Limit Exceeded"
    Integer timeMs,
    Integer memoryKb
) {}
