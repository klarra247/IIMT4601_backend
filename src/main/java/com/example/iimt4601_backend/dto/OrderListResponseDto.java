package com.example.iimt4601_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderListResponseDto {
    private List<OrderDto> orders;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}

