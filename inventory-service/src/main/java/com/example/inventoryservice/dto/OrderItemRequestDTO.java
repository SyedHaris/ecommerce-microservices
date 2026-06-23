package com.example.inventoryservice.dto;

public record OrderItemRequestDTO(
        Long id,
        Integer quantity
) {
}
