package com.example.inventoryservice.listener;

import com.example.inventoryservice.dto.OrderCreatedEventDTO;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "${kafka-topics.order_created_topic}", groupId = "inventory-consumer-group",
            properties = {
                "spring.json.type.mapping:com.example.orderservice.dto.OrderCreatedEventDTO:com.example.inventoryservice.dto.OrderCreatedEventDTO"
            }
    )
    public void orderCreated(OrderCreatedEventDTO orderCreatedEventDTO) {
        log.info("Received order created message: {}", orderCreatedEventDTO);
        inventoryService.reserve(orderCreatedEventDTO);
    }

    @KafkaListener(topics = "${kafka-topics.order_purchase_failed_topic}", groupId = "inventory-consumer-group",
            properties = {
                    "spring.json.type.mapping:com.example.paymentservice.dto.OrderCreatedEventDTO:com.example.inventoryservice.dto.OrderCreatedEventDTO"
            }
    )
    public void orderPurchaseFailed(OrderCreatedEventDTO orderCreatedEventDTO) {
        log.info("Received order purchase failed message: {}", orderCreatedEventDTO);
        inventoryService.release(orderCreatedEventDTO);
    }

}
