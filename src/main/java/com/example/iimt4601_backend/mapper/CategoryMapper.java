package com.example.iimt4601_backend.mapper;

import com.example.iimt4601_backend.dto.CategoryDto;
import com.example.iimt4601_backend.dto.CategoryTreeDto;
import com.example.iimt4601_backend.entity.Category;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    /**
     * Category Entity를 CategoryDto로 변환
     */
    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setBannerImage(category.getBannerImage());
        dto.setTotalProducts(category.getProducts() != null ? category.getProducts().size() : 0);

        return dto;
    }

    /**
     * CategoryDto를 Category Entity로 변환
     */
    public Category toEntity(CategoryDto dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setBannerImage(dto.getBannerImage());

        return category;
    }

    /**
     * Category Entity를 CategoryTreeDto로 변환 (계층 구조 포함)
     */
    public CategoryTreeDto toCategoryTreeDto(Category category, String baseUrl, Long selectedId) {
        if (category == null) {
            return null;
        }

        CategoryTreeDto treeDto = new CategoryTreeDto();
        treeDto.setId(category.getId());
        treeDto.setName(category.getName());
        treeDto.setLink(baseUrl + "/category/" + category.getId());
        treeDto.setCount(category.getProducts() != null ? category.getProducts().size() : 0);
        treeDto.setSelected(category.getId().equals(selectedId));

        return treeDto;
    }

    /**
     * 카테고리 목록을 트리 구조로 변환 (최상위 카테고리만 처리)
     */
    public List<CategoryTreeDto> toCategoryTreeList(List<Category> categories, String baseUrl, Long selectedId) {
        if (categories == null) {
            return new ArrayList<>();
        }

        // 최상위 카테고리만 필터링 (parent가 null인 카테고리)
        return categories.stream()
                .map(category -> toCategoryTreeDto(category, baseUrl, selectedId))
                .collect(Collectors.toList());
    }
}