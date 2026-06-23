package com.example.paymentservice.service.impl;

import com.example.paymentservice.dto.OrderCreatedEventDTO;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.enums.PaymentMethod;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.exception.PaymentFailedException;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka-topics.order_purchased_topic}")
    private String ORDER_PURCHASED_TOPIC;

    @Value("${kafka-topics.order_purchase_failed_topic}")
    private String ORDER_PURCHASE_FAILED;

    @Override
    @Transactional
    public void processPayment(OrderCreatedEventDTO orderCreatedEventDTO) {
        try {
            if (Objects.isNull(orderCreatedEventDTO)) {
                return;
            }
            // This condition is just to simulate payment failure and observe saga compensatory flow.
            if (BigDecimal.valueOf(200000).compareTo(orderCreatedEventDTO.amount()) < 0) {
                createPayment(orderCreatedEventDTO, PaymentStatus.FAILED);
                throw new PaymentFailedException("Payment failed for order ID: " + orderCreatedEventDTO.orderId());
            }
            createPayment(orderCreatedEventDTO, PaymentStatus.PAID);
            kafkaTemplate.send(ORDER_PURCHASED_TOPIC, orderCreatedEventDTO);
        } catch (PaymentFailedException e) {
            log.error("Exception occurred while payment", e);
            kafkaTemplate.send(ORDER_PURCHASE_FAILED, orderCreatedEventDTO);
        }
    }

    private void createPayment(OrderCreatedEventDTO orderCreatedEventDTO, PaymentStatus paymentStatus) {
        Payment payment = new Payment();
        payment.setAmount(orderCreatedEventDTO.amount());
        payment.setOrderId(orderCreatedEventDTO.orderId());
        payment.setStatus(paymentStatus);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentMethod(PaymentMethod.CARD);
        paymentRepository.save(payment);
    }

}
