package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.OrderCreatedEventDTO;

public interface InventoryService {

    void reserve(OrderCreatedEventDTO orderCreatedEventDTO);

    void release(OrderCreatedEventDTO orderCreatedEventDTO);

}
