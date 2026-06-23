package com.example.orderservice.listener;

import com.example.orderservice.dto.OrderCreatedEventDTO;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "${kafka-topics.order_purchased_topic}", groupId = "order-consumer-group",
            properties = {
                "spring.json.type.mapping:com.example.paymentservice.dto.OrderCreatedEventDTO:com.example.orderservice.dto.OrderCreatedEventDTO"
            }
    )
    public void orderPurchased(OrderCreatedEventDTO orderCreatedEventDTO) {
        log.info("Received order purchased message: {}", orderCreatedEventDTO);
        orderService.updateStatus(orderCreatedEventDTO.orderId(), orderCreatedEventDTO.userId(), OrderStatus.CONFIRMED);
    }

    @KafkaListener(topics = "${kafka-topics.order_purchase_failed_topic}", groupId = "order-consumer-group",
            properties = {
                    "spring.json.type.mapping:com.example.paymentservice.dto.OrderCreatedEventDTO:com.example.orderservice.dto.OrderCreatedEventDTO"
            }
    )
    public void orderPurchaseFailed(OrderCreatedEventDTO orderCreatedEventDTO) {
        log.info("Received order purchase failed message: {}", orderCreatedEventDTO);
        orderService.updateStatus(orderCreatedEventDTO.orderId(), orderCreatedEventDTO.userId(), OrderStatus.CANCELLED);
    }

    @KafkaListener(topics = "${kafka-topics.inventory_reservation_failed_topic}", groupId = "order-consumer-group",
            properties = {
                    "spring.json.type.mapping:com.example.inventoryservice.dto.OrderCreatedEventDTO:com.example.orderservice.dto.OrderCreatedEventDTO"
            }
    )
    public void inventoryReservationFailed(OrderCreatedEventDTO orderCreatedEventDTO) {
        log.info("Received inventory reservation failed message: {}", orderCreatedEventDTO);
        orderService.updateStatus(orderCreatedEventDTO.orderId(), orderCreatedEventDTO.userId(), OrderStatus.CANCELLED);
    }

}
