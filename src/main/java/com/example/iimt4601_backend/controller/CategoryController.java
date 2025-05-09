package com.example.iimt4601_backend.controller;

import com.example.iimt4601_backend.dto.CategoryDto;
import com.example.iimt4601_backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 모든 카테고리 조회
     */
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * ID로 카테고리 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        CategoryDto category = categoryService.getCategoryById(id);
        if (category != null) {
            return ResponseEntity.ok(category);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 카테고리별 상품 수 조회
     */
    @GetMapping("/{id}/product-count")
    public ResponseEntity<Integer> getCategoryProductCount(@PathVariable Long id) {
        Integer count = categoryService.getProductCountByCategory(id);
        return ResponseEntity.ok(count);
    }

}