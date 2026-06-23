package com.example.orderservice.dto;

import com.example.orderservice.entity.Order;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEventDTO(
        Long orderId,
        Long userId,
        BigDecimal amount,
        List<OrderItemRequestDTO> orderItems
) {
    public static OrderCreatedEventDTO fromOrder(Order order) {
        List<OrderItemRequestDTO> orderItems = order.getItems()
                .stream()
                .map(OrderItemRequestDTO::fromOrderItem)
                .toList();
        return new OrderCreatedEventDTO(
                order.getId(),
                order.getUserId(),
                order.getAmount(),
                orderItems
        );
    }
}
