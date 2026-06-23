package com.example.paymentservice.listener;

import com.example.paymentservice.dto.OrderCreatedEventDTO;
import com.example.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KafkaEventConsumerTest {

    private final PaymentService paymentService = mock(PaymentService.class);
    private final KafkaEventConsumer consumer = new KafkaEventConsumer(paymentService);

    @Test
    void orderCreatedProcessesPayment() {
        OrderCreatedEventDTO event = new OrderCreatedEventDTO(1L, 2L, BigDecimal.TEN, List.of());

        consumer.orderCreated(event);

        verify(paymentService).processPayment(event);
    }
}
