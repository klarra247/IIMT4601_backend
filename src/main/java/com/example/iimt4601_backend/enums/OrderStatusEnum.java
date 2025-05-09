package com.example.iimt4601_backend.enums;

public enum OrderStatusEnum {
    PENDING("Pending for Confirmation"),
    PAID("Payment Complete"),
    PREPARING("Preparing Order"),
    READY("Ready for Pickup"),
    DELIVERED("Delivered"),
    CANCELED("Order Cancelled"),
    REFUNDED("Refunded");

    private final String displayName;

    OrderStatusEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}