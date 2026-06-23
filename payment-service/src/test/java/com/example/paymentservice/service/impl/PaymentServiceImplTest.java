package com.example.paymentservice.service.impl;

import com.example.paymentservice.dto.OrderCreatedEventDTO;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.enums.PaymentMethod;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "ORDER_PURCHASED_TOPIC", "order_purchased");
        ReflectionTestUtils.setField(paymentService, "ORDER_PURCHASE_FAILED", "order_failed");
    }

    @Test
    void processPaymentCreatesPaidPaymentAndPublishesSuccess() {
        OrderCreatedEventDTO event = new OrderCreatedEventDTO(1L, 2L, BigDecimal.valueOf(100), List.of());

        paymentService.processPayment(event);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(1L);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("100");
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(captor.getValue().getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(captor.getValue().getTransactionId()).isNotBlank();
        verify(kafkaTemplate).send("order_purchased", event);
        verify(kafkaTemplate, never()).send("order_failed", event);
    }

    @Test
    void processPaymentCreatesFailedPaymentAndPublishesFailure() {
        OrderCreatedEventDTO event = new OrderCreatedEventDTO(1L, 2L, BigDecimal.valueOf(200001), List.of());

        paymentService.processPayment(event);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(kafkaTemplate).send("order_failed", event);
        verify(kafkaTemplate, never()).send("order_purchased", event);
    }

    @Test
    void processPaymentIgnoresNullEvent() {
        paymentService.processPayment(null);

        verify(paymentRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(kafkaTemplate, never()).send(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }
}
