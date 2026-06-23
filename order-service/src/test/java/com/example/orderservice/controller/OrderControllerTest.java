package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderItemRequestDTO;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;
import com.example.orderservice.enums.OrderStatus;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    private final OrderService orderService = mock(OrderService.class);
    private final OrderController controller = new OrderController(orderService);

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("7", null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createDelegatesWithAuthenticatedUserId() {
        OrderRequestDTO request = new OrderRequestDTO("address", "instructions", List.of(new OrderItemRequestDTO(1L, 2)));
        OrderResponseDTO expected = response(10L);
        when(orderService.create(request, 7L)).thenReturn(expected);

        assertThat(controller.create(request)).isEqualTo(expected);
    }

    @Test
    void getOrderDelegatesWithAuthenticatedUserId() {
        OrderResponseDTO expected = response(10L);
        when(orderService.getOrder(10L, 7L)).thenReturn(expected);

        assertThat(controller.getOrder(10L)).isEqualTo(expected);
    }

    @Test
    void getOrdersDelegatesWithAuthenticatedUserId() {
        when(orderService.getOrders(7L, 0, 10)).thenReturn(List.of(response(10L)));

        assertThat(controller.getOrders(0, 10)).extracting(OrderResponseDTO::id).containsExactly(10L);
    }

    @Test
    void getAllOrdersDelegatesWithoutAuthenticatedUserId() {
        when(orderService.getAllOrders(0, 10)).thenReturn(List.of(response(11L)));

        assertThat(controller.getAllOrders(0, 10)).extracting(OrderResponseDTO::id).containsExactly(11L);
    }

    private static OrderResponseDTO response(Long id) {
        return new OrderResponseDTO(id, "address", "instructions", BigDecimal.TEN, OrderStatus.PENDING, null, List.of());
    }
}
