package com.example.iimt4601_backend.dto;

import com.example.iimt4601_backend.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String title;
    private String price;
    private String image;
    private CategoryDto category;
    private Boolean isNew;
    private Integer quantity; // 수량 필드 추가

}
