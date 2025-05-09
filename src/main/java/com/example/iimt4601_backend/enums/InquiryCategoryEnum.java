package com.example.iimt4601_backend.enums;

import lombok.Getter;

@Getter
public enum InquiryCategoryEnum {
    PRODUCT("Product Inquiry"),
    ORDER("Order Inquiry"),
    PAYMENT("Payment Inquiry"),
    RETURN("Return/Exchange"),
    ETC("Others");

    private final String displayName;

    InquiryCategoryEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}