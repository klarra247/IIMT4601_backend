package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String discount;
    private String description;
    private Set<String> options;
    private List<String> images;
    private String category;
    private Boolean isNew;
    private Boolean isBest;

}