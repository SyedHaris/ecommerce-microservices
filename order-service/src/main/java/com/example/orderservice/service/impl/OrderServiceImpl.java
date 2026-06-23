package com.example.orderservice.service.impl;

import com.example.orderservice.client.InventoryServiceClient;
import com.example.orderservice.dto.*;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Value("${kafka-topics.order_created_topic}")
    public String ORDER_CREATED;

    private final InventoryServiceClient inventoryServiceClient;

    private final OrderRepository orderRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public OrderResponseDTO create(OrderRequestDTO orderRequestDTO, Long userId) {
        Set<Long> productIds = orderRequestDTO.orderItems()
                .stream().map(OrderItemRequestDTO::id)
                .collect(Collectors.toSet());
        List<ProductDetailsResponseDTO> products = inventoryServiceClient.getProductsByIds(productIds);
        Map<Long, ProductDetailsResponseDTO> idProductMap = products.stream()
                .collect(Collectors.toMap(ProductDetailsResponseDTO::id, Function.identity()));
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order();
        order.setAddress(orderRequestDTO.address());
        order.setInstructions(orderRequestDTO.instructions());
        order.setStatus(OrderStatus.PENDING);
        order.setUserId(userId);
        for (OrderItemRequestDTO orderItemRequestDTO : orderRequestDTO.orderItems()) {
            ProductDetailsResponseDTO product = idProductMap.get(orderItemRequestDTO.id());
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.id());
            orderItem.setProductName(product.name());
            orderItem.setPrice(product.price());
            BigDecimal totalPrice = product.price().multiply(BigDecimal.valueOf(orderItemRequestDTO.quantity()));
            orderItem.setTotalPrice(totalPrice);
            orderItem.setQuantity(orderItemRequestDTO.quantity());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(totalPrice);
        }
        order.setAmount(totalAmount);
        order.setItems(orderItems);

        orderRepository.save(order);

        kafkaTemplate.send(ORDER_CREATED, OrderCreatedEventDTO.fromOrder(order));

        return OrderResponseDTO.from(order);
    }

    @Override
    @Transactional
    @CachePut(value = "orders", key = "#userId + '_' + #orderId")
    public OrderResponseDTO updateStatus(Long orderId, Long userId, OrderStatus orderStatus) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        order.setStatus(orderStatus);
        return OrderResponseDTO.from(order);
    }

    @Override
    @Cacheable(value = "orders", key = "#userId + '_' + #orderId")
    public OrderResponseDTO getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        return OrderResponseDTO.from(order);
    }

    @Override
    public List<OrderResponseDTO> getOrders(Long userId, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        List<Order> orders = orderRepository.findByUserId(userId, pageRequest);
        return orders.stream()
                .map(OrderResponseDTO::from)
                .toList();
    }

    @Override
    public List<OrderResponseDTO> getAllOrders(Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        List<Order> orders = orderRepository.findBy(pageRequest);
        return orders.stream()
                .map(OrderResponseDTO::from)
                .toList();
    }

}
