package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequestDTO(
        @Email
        String email,
        @Size(min = 6)
        String password,
        @NotBlank
        String username
) {
}
