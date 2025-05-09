package com.example.iimt4601_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class OrderDto {
    private Long id;
    private String orderNumber;
    private LocalDate pickupDate;
    private LocalTime pickupTime;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime orderDate;
    private String paymentMethod;
    private List<ProductDto> product;
}
