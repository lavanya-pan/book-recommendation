package com.sds.bookrecommendation.dto;

public record AuthResponse(
        Long userId,
        String name,
        String email,
        String token,
        String tokenType
) {
}