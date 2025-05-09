package com.example.iimt4601_backend.dto;

import com.example.iimt4601_backend.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String productName;
    private BigDecimal price;
    private String description;
    private String sku;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Category category;
    private Set<String> tags = new HashSet<>();
    private Set<String> options = new HashSet<>();
    private String nutritionInfo;
    private String size;
    private Double weight;
    private String ingredients;
    private String storageMethod;
    private String shelfLife;
    private Boolean isAvailable;
    private Double discountPercentage;
    private BigDecimal originalPrice;
    private List<String> images = new ArrayList<>();
    private String thumbnail;
    private Double rating;
    private Integer reviewCount;
    private Boolean isNew;

    private Boolean requiresPreOrder;
    private Integer preOrderHours;
    private String patissier;
    private Boolean isCustomizable;
    private Boolean seasonal;
    private String seasonInfo;

    // 계산된 필드 - 할인율이 적용된 최종 판매가
    private BigDecimal finalPrice;


    // 계산된 필드 - 주문 안내 메시지
    private String orderInfo;
}