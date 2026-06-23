package com.example.userservice.dto;

import java.util.Date;

public record LoginResponseDTO(
        String token,
        Date createdAt,
        Date expiresAt
) {
}
