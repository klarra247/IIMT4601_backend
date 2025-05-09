package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class OrderTimeSeriesDto {
    private String date;
    private BigDecimal sales;
    private Integer orderCount;
}
