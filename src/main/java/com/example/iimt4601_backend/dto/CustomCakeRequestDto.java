package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Custom Cake Request DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomCakeRequestDto {
    private String designId;
    private String designName;
    private String basePrice;
}
