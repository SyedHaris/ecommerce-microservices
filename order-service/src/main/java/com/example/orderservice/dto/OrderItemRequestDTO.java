package com.example.orderservice.dto;

import com.example.orderservice.entity.OrderItem;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDTO(
        @NotNull
        Long id,
        @NotNull
        @Min(1)
        Integer quantity
) {
        public static OrderItemRequestDTO fromOrderItem(OrderItem orderItem) {
                return new OrderItemRequestDTO(
                        orderItem.getProductId(),
                        orderItem.getQuantity()
                );
        }
}
