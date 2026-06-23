package com.example.paymentservice.listener;

import com.example.paymentservice.dto.OrderCreatedEventDTO;
import com.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${kafka-topics.inventory_reserved_topic}", groupId = "payment-consumer-group",
            properties = {
                    "spring.json.type.mapping:com.example.inventoryservice.dto.OrderCreatedEventDTO:com.example.paymentservice.dto.OrderCreatedEventDTO"
            }
    )
    public void orderCreated(OrderCreatedEventDTO orderCreatedEventDTO) {
        log.info("Received message: {}", orderCreatedEventDTO);
        paymentService.processPayment(orderCreatedEventDTO);
    }

}