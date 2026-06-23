package com.example.paymentservice.service;

import com.example.paymentservice.dto.OrderCreatedEventDTO;

public interface PaymentService {

    void processPayment(OrderCreatedEventDTO orderCreatedEventDTO);

}
