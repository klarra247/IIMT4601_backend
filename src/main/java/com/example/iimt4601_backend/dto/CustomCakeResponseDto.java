package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Custom Cake Response DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomCakeResponseDto {
    private Boolean success;
    private String customizationId;
    private String redirectUrl;
}
