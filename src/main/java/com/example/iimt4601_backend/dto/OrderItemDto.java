package com.example.iimt4601_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private String thumbnail;
    private Integer quantity;
    private BigDecimal price;
    private Double discountPercentage;
    private String options;
}
