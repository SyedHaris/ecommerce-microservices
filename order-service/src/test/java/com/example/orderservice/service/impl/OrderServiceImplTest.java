package com.example.orderservice.service.impl;

import com.example.orderservice.client.InventoryServiceClient;
import com.example.orderservice.dto.OrderItemRequestDTO;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;
import com.example.orderservice.dto.ProductDetailsResponseDTO;
import com.example.orderservice.entity.Order;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.repository.OrderRepository;
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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private InventoryServiceClient inventoryServiceClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "ORDER_CREATED", "order_created");
    }

    @Test
    void createBuildsOrderAndPublishesEvent() {
        OrderRequestDTO request = new OrderRequestDTO(
                "221B Baker Street",
                "leave at door",
                List.of(new OrderItemRequestDTO(10L, 2), new OrderItemRequestDTO(20L, 1))
        );
        when(inventoryServiceClient.getProductsByIds(Set.of(10L, 20L))).thenReturn(List.of(
                new ProductDetailsResponseDTO(10L, "Keyboard", "Mechanical", BigDecimal.valueOf(50), "KB", 4),
                new ProductDetailsResponseDTO(20L, "Mouse", "Wireless", BigDecimal.valueOf(25), "MS", 7)
        ));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(99L);
            return order;
        });

        OrderResponseDTO response = orderService.create(request, 7L);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.amount()).isEqualByComparingTo("125");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.orderItems()).hasSize(2);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getItems()).allSatisfy(item -> assertThat(item.getOrder()).isSameAs(orderCaptor.getValue()));
        verify(kafkaTemplate).send("order_created", com.example.orderservice.dto.OrderCreatedEventDTO.fromOrder(orderCaptor.getValue()));
    }

    @Test
    void updateStatusUpdatesExistingOrder() {
        Order order = order(44L, 7L, OrderStatus.PENDING);
        when(orderRepository.findByIdAndUserId(44L, 7L)).thenReturn(Optional.of(order));

        OrderResponseDTO response = orderService.updateStatus(44L, 7L, OrderStatus.CONFIRMED);

        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateStatusFailsWhenOrderMissing() {
        when(orderRepository.findByIdAndUserId(44L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(44L, 7L, OrderStatus.CANCELLED))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    void getOrderReturnsExistingOrder() {
        when(orderRepository.findByIdAndUserId(44L, 7L)).thenReturn(Optional.of(order(44L, 7L, OrderStatus.CONFIRMED)));

        OrderResponseDTO response = orderService.getOrder(44L, 7L);

        assertThat(response.id()).isEqualTo(44L);
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void listMethodsDelegateToRepository() {
        when(orderRepository.findByUserId(any(), any())).thenReturn(List.of(order(1L, 7L, OrderStatus.PENDING)));
        when(orderRepository.findBy(any())).thenReturn(List.of(order(2L, 8L, OrderStatus.CONFIRMED)));

        assertThat(orderService.getOrders(7L, 0, 10)).extracting(OrderResponseDTO::id).containsExactly(1L);
        assertThat(orderService.getAllOrders(0, 10)).extracting(OrderResponseDTO::id).containsExactly(2L);
    }

    private static Order order(Long id, Long userId, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        order.setAddress("address");
        order.setInstructions("instructions");
        order.setAmount(BigDecimal.TEN);
        order.setStatus(status);
        order.setItems(List.of());
        return order;
    }
}
