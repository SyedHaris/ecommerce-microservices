package com.example.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequestDTO(
        @NotBlank
        String address,
        String instructions,
        @NotEmpty
        List<OrderItemRequestDTO> orderItems
) {
}
