package com.example.inventoryservice.listener;

import com.example.inventoryservice.dto.OrderCreatedEventDTO;
import com.example.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaEventConsumerTest {

    private final InventoryService inventoryService = mock(InventoryService.class);
    private final KafkaEventConsumer consumer = new KafkaEventConsumer(inventoryService);

    @Test
    void orderCreatedReservesInventory() {
        OrderCreatedEventDTO event = event();

        consumer.orderCreated(event);

        verify(inventoryService).reserve(event);
    }

    @Test
    void orderPurchaseFailedReleasesInventory() {
        OrderCreatedEventDTO event = event();

        consumer.orderPurchaseFailed(event);

        verify(inventoryService).release(event);
    }

    private static OrderCreatedEventDTO event() {
        return new OrderCreatedEventDTO(1L, 2L, BigDecimal.TEN, List.of());
    }
}
