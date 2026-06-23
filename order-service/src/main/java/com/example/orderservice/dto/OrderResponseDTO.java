package com.example.orderservice.dto;

import com.example.orderservice.entity.Order;
import com.example.orderservice.enums.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        String address,
        String instructions,
        BigDecimal amount,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemResponseDTO> orderItems
) implements Serializable {
    public static OrderResponseDTO from(Order order) {
        List<OrderItemResponseDTO> orderItems = order.getItems()
                .stream()
                .map(OrderItemResponseDTO::fromOrderItem)
                .toList();
        return new OrderResponseDTO(
                order.getId(),
                order.getAddress(),
                order.getInstructions(),
                order.getAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                orderItems
        );
    }
}
