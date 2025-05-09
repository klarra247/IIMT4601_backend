package com.example.iimt4601_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderRequestDto {
    private List<OrderItemDto> items;
    private String pickupDate;
    private String pickupTime;
//    private String specialInstructions;
    private String paymentMethod;
    private String paymentProofUrl;
    private BigDecimal totalAmount;
}

