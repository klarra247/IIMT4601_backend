package com.example.iimt4601_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductListResponseDto {
    private List<ProductResponseDto> products;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}

