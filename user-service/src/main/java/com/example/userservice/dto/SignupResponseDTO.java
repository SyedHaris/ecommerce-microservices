package com.example.userservice.dto;

import com.example.userservice.constans.UserRole;

public record SignupResponseDTO(
        Long id,
        String email,
        String username,
        UserRole role
) {
}
