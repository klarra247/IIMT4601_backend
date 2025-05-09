package com.example.iimt4601_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDetailDto {
    private Long id;
    private String orderNumber;
    private UserMinimalDto user;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime orderDate;
    private String paymentMethod;
    private String trackingNumber;
    private List<OrderItemDto> items;
}

