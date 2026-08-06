package com.preppilot.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String fullName,
        String email
) {}
