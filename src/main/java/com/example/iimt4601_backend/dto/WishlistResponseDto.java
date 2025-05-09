package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class WishlistResponseDto {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String imageUrl;
}


