package com.example.inventoryservice.exception;

public class InventoryReservationFailed extends RuntimeException {
    public InventoryReservationFailed(String message) {
        super(message);
    }
}
