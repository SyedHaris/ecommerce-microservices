package com.example.inventoryservice.service.impl;

import com.example.inventoryservice.dto.OrderCreatedEventDTO;
import com.example.inventoryservice.dto.OrderItemRequestDTO;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.entity.Product;
import com.example.inventoryservice.exception.InventoryReservationFailed;
import com.example.inventoryservice.service.InventoryService;
import com.example.inventoryservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final ProductService productService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka-topics.inventory_reserved_topic}")
    private String INVENTORY_RESERVED_TOPIC;

    @Value("${kafka-topics.inventory_reservation_failed_topic}")
    private String INVENTORY_RESERVATION_FAILED_TOPIC;

    @Override
    @Transactional
    public void reserve(OrderCreatedEventDTO orderCreatedEventDTO) {
        try {
            Map<Long, Product> idProductMap = prepareIdProductMap(orderCreatedEventDTO);
            orderCreatedEventDTO.orderItems().forEach(orderItemRequestDTO -> {
                Product product = idProductMap.get(orderItemRequestDTO.id());
                if (Objects.nonNull(product)) {
                    Inventory inventory = product.getInventory();
                    if (Objects.nonNull(inventory) && inventory.getAvailableQuantity() >= orderItemRequestDTO.quantity()) {
                        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - orderItemRequestDTO.quantity());
                    } else {
                        throw new InventoryReservationFailed("Failed to reserve inventory for order ID: " + orderCreatedEventDTO.orderId());
                    }
                }
            });
            kafkaTemplate.send(INVENTORY_RESERVED_TOPIC, orderCreatedEventDTO);
        } catch (InventoryReservationFailed e) {
            log.error("Exception occurred while inventory reservation", e);
            kafkaTemplate.send(INVENTORY_RESERVATION_FAILED_TOPIC, orderCreatedEventDTO);
        }
    }

    @Override
    @Transactional
    public void release(OrderCreatedEventDTO orderCreatedEventDTO) {
        Map<Long, Product> idProductMap = prepareIdProductMap(orderCreatedEventDTO);
        orderCreatedEventDTO.orderItems().forEach(orderItemRequestDTO -> {
            Product product = idProductMap.get(orderItemRequestDTO.id());
            if (Objects.nonNull(product)) {
                Inventory inventory = product.getInventory();
                if (Objects.nonNull(inventory)) {
                    inventory.setAvailableQuantity(inventory.getAvailableQuantity() + orderItemRequestDTO.quantity());
                }
            }
        });
    }

    private Map<Long, Product> prepareIdProductMap(OrderCreatedEventDTO orderCreatedEventDTO) {
        Set<Long> productIds = orderCreatedEventDTO.orderItems()
                .stream()
                .map(OrderItemRequestDTO::id).collect(Collectors.toSet());
        return productService.getProductsByIds(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

}
