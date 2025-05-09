package com.example.iimt4601_backend.service;

import com.example.iimt4601_backend.dto.CategoryDto;
import com.example.iimt4601_backend.entity.Category;
import com.example.iimt4601_backend.repository.CategoryRepository;
import com.example.iimt4601_backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * 모든 카테고리 조회
     * @return 모든 카테고리 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(this::convertToCategoryDto)
                .collect(Collectors.toList());
    }

    /**
     * ID로 카테고리 조회
     * @param id 카테고리 ID
     * @return 카테고리 DTO 또는 null(존재하지 않는 경우)
     */
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        return categoryOpt.map(this::convertToCategoryDto).orElse(null);
    }

    /**
     * 카테고리별 상품 수 조회
     * @param categoryId 카테고리 ID
     * @return 해당 카테고리의 상품 수
     */
    @Transactional(readOnly = true)
    public Integer getProductCountByCategory(Long categoryId) {
        Long count = productRepository.countByCategoryId(categoryId);
        return count != null ? count.intValue() : 0;
    }

    /**
     * Category 엔티티를 CategoryDto로 변환
     * @param category 카테고리 엔티티
     * @return 카테고리 DTO
     */
    private CategoryDto convertToCategoryDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setBannerImage(category.getBannerImage());

        // 상품 수 설정 (선택적)
        Long productCount = productRepository.countByCategoryId(category.getId());
        dto.setTotalProducts(productCount != null ? productCount.intValue() : 0);

        return dto;
    }
}