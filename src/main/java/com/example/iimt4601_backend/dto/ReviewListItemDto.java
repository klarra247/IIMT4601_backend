package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Long userId;
    private String username;
    private Integer rating;
    private String title;
    private Boolean visible;
    private LocalDateTime createdAt;
}