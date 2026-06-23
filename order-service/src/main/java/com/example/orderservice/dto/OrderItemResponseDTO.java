package com.example.orderservice.dto;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;

import java.io.Serializable;
import java.math.BigDecimal;

public record OrderItemResponseDTO(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalPrice
) implements Serializable {
    public static OrderItemResponseDTO fromOrderItem(OrderItem orderItem) {
        return new OrderItemResponseDTO(
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getQuantity(),
                orderItem.getPrice(),
                orderItem.getTotalPrice()
        );
    }
}
