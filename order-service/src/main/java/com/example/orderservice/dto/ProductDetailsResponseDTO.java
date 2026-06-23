package com.example.orderservice.dto;


import java.math.BigDecimal;

public record ProductDetailsResponseDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String sku,
        Integer quantity
) {
}
