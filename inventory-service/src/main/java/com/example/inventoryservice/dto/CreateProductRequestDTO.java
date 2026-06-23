package com.example.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequestDTO(
        @NotBlank
        String name,
        @NotBlank
        String description,
        @Min(1)
        @NotNull
        BigDecimal price,
        @NotBlank
        String sku,
        @Min(0)
        @NotNull
        Integer quantity
) {
}
