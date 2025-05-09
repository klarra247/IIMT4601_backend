package com.example.iimt4601_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListResponseDto {
    private Integer total;
    private Integer page;
    private Integer limit;
    private List<ReviewDto> reviews;
}
