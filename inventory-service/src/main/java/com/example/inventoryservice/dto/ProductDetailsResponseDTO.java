package com.example.inventoryservice.dto;


import com.example.inventoryservice.entity.Product;

import java.math.BigDecimal;

public record ProductDetailsResponseDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String sku,
        Integer quantity
) {
    public static ProductDetailsResponseDTO from(Product product) {
        return new ProductDetailsResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.getInventory().getAvailableQuantity()
        );
    }
}
