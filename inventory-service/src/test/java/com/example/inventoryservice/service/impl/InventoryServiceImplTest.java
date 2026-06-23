package com.example.inventoryservice.service.impl;

import com.example.inventoryservice.dto.OrderCreatedEventDTO;
import com.example.inventoryservice.dto.OrderItemRequestDTO;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.entity.Product;
import com.example.inventoryservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private ProductService productService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inventoryService, "INVENTORY_RESERVED_TOPIC", "inventory_reserved");
        ReflectionTestUtils.setField(inventoryService, "INVENTORY_RESERVATION_FAILED_TOPIC", "inventory_failed");
    }

    @Test
    void reserveReducesStockAndPublishesSuccess() {
        Product product = product(10L, 5);
        OrderCreatedEventDTO event = event(10L, 2);
        when(productService.getProductsByIds(Set.of(10L))).thenReturn(List.of(product));

        inventoryService.reserve(event);

        assertThat(product.getInventory().getAvailableQuantity()).isEqualTo(3);
        verify(kafkaTemplate).send("inventory_reserved", event);
        verify(kafkaTemplate, never()).send("inventory_failed", event);
    }

    @Test
    void reservePublishesFailureWhenStockIsInsufficient() {
        Product product = product(10L, 1);
        OrderCreatedEventDTO event = event(10L, 2);
        when(productService.getProductsByIds(Set.of(10L))).thenReturn(List.of(product));

        inventoryService.reserve(event);

        assertThat(product.getInventory().getAvailableQuantity()).isEqualTo(1);
        verify(kafkaTemplate).send("inventory_failed", event);
        verify(kafkaTemplate, never()).send("inventory_reserved", event);
    }

    @Test
    void reserveIgnoresUnknownProductButStillPublishesSuccess() {
        OrderCreatedEventDTO event = event(10L, 2);
        when(productService.getProductsByIds(Set.of(10L))).thenReturn(List.of());

        inventoryService.reserve(event);

        verify(kafkaTemplate).send("inventory_reserved", event);
    }

    @Test
    void releaseRestoresStock() {
        Product product = product(10L, 1);
        OrderCreatedEventDTO event = event(10L, 2);
        when(productService.getProductsByIds(Set.of(10L))).thenReturn(List.of(product));

        inventoryService.release(event);

        assertThat(product.getInventory().getAvailableQuantity()).isEqualTo(3);
    }

    @Test
    void releaseIgnoresProductWithoutInventory() {
        Product product = new Product();
        product.setId(10L);
        OrderCreatedEventDTO event = event(10L, 2);
        when(productService.getProductsByIds(Set.of(10L))).thenReturn(List.of(product));

        inventoryService.release(event);

        assertThat(product.getInventory()).isNull();
    }

    private static OrderCreatedEventDTO event(Long productId, int quantity) {
        return new OrderCreatedEventDTO(1L, 2L, BigDecimal.TEN, List.of(new OrderItemRequestDTO(productId, quantity)));
    }

    private static Product product(Long id, int quantity) {
        Product product = new Product();
        product.setId(id);
        Inventory inventory = new Inventory();
        inventory.setAvailableQuantity(quantity);
        product.setInventory(inventory);
        return product;
    }
}
