package com.example.orderservice.service;

import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;
import com.example.orderservice.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponseDTO create(OrderRequestDTO orderRequestDTO, Long userId);

    OrderResponseDTO updateStatus(Long orderId, Long userId, OrderStatus orderStatus);

    OrderResponseDTO getOrder(Long orderId, Long userId);

    List<OrderResponseDTO> getOrders(Long userId, Integer page, Integer size);

    List<OrderResponseDTO> getAllOrders(Integer page, Integer size);

}
