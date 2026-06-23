package com.example.inventoryservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEventDTO(
        Long orderId,
        Long userId,
        BigDecimal amount,
        List<OrderItemRequestDTO> orderItems
) {
}
