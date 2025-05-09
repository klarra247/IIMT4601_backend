package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatCardDto {
    private String label;
    private Object currentValue;
    private Object previousValue;
    private Double changePercentage;
    private String period; // "daily", "weekly", "monthly", "yearly"
}