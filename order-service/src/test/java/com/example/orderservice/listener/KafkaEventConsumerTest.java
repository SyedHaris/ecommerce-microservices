package com.example.orderservice.listener;

import com.example.orderservice.dto.OrderCreatedEventDTO;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaEventConsumerTest {

    private final OrderService orderService = mock(OrderService.class);
    private final KafkaEventConsumer consumer = new KafkaEventConsumer(orderService);

    @Test
    void orderPurchasedConfirmsOrder() {
        OrderCreatedEventDTO event = event();

        consumer.orderPurchased(event);

        verify(orderService).updateStatus(10L, 20L, OrderStatus.CONFIRMED);
    }

    @Test
    void orderPurchaseFailedCancelsOrder() {
        OrderCreatedEventDTO event = event();

        consumer.orderPurchaseFailed(event);

        verify(orderService).updateStatus(10L, 20L, OrderStatus.CANCELLED);
    }

    @Test
    void inventoryReservationFailedCancelsOrder() {
        OrderCreatedEventDTO event = event();

        consumer.inventoryReservationFailed(event);

        verify(orderService).updateStatus(10L, 20L, OrderStatus.CANCELLED);
    }

    private static OrderCreatedEventDTO event() {
        return new OrderCreatedEventDTO(10L, 20L, BigDecimal.TEN, List.of());
    }
}
