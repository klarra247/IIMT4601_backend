package com.example.iimt4601_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserDetailDto {
    private Long id;
    private String userName;
    private String name;
    private String email;
    private String phoneNumber;
    private Boolean isActive;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginDate;
    private String shippingAddress;
    private Boolean emailNotifications;
    private Boolean marketingConsent;
    private List<OrderMinimalDto> recentOrders;
}

