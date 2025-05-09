package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailDto {
    private Long id;
    private ProductDto product;
    private UserDto user;
    private Integer rating;
    private String title;
    private String content;
    private List<String> images;
    private Boolean visible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}