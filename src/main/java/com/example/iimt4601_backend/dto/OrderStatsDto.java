package com.example.iimt4601_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class OrderStatsDto {
    private BigDecimal totalSales;
    private Integer totalOrders;
    private BigDecimal averageOrderValue;
    private Map<String, Integer> ordersByStatus;
    private List<OrderTimeSeriesDto> timeSeriesData;
}

