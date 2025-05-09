package com.example.iimt4601_backend.dto;
import com.example.iimt4601_backend.entity.Category;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {
    @NotBlank(message = "케이크명은 필수입니다")
    private String productName;

    @NotNull(message = "가격은 필수입니다")
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다")
    private BigDecimal price;

    private String description;
    private String sku;
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
    private Boolean isNew;

    private Boolean requiresPreOrder;
    private Integer preOrderHours;
    private String patissier;
    private Boolean isCustomizable;
    private Boolean seasonal;
    private String seasonInfo;
    private Integer reorderPoint;
}