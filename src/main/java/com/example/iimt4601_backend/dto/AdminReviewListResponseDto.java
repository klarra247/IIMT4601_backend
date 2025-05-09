package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewListResponseDto {
    private List<ReviewListItemDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}