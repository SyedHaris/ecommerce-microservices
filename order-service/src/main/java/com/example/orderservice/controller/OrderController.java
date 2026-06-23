package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public OrderResponseDTO create(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        return orderService.create(orderRequestDTO, AuthUtil.getAuthenticatedUserId());
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public OrderResponseDTO getOrder(@PathVariable("orderId") Long orderId) {
        return orderService.getOrder(orderId, AuthUtil.getAuthenticatedUserId());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<OrderResponseDTO> getOrders(@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return orderService.getOrders(AuthUtil.getAuthenticatedUserId(), page, size);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public List<OrderResponseDTO> getAllOrders(@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return orderService.getAllOrders(page, size);
    }

}
