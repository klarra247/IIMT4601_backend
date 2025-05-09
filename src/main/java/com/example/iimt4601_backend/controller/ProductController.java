package com.example.iimt4601_backend.controller;

import com.example.iimt4601_backend.dto.*;
import com.example.iimt4601_backend.entity.Product;
import com.example.iimt4601_backend.exception.ResourceNotFoundException;
import com.example.iimt4601_backend.repository.ProductRepository;
import com.example.iimt4601_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/gallery-items")
    public ResponseEntity<List<GalleryItemDto>> getGalleryItems() {
        List<GalleryItemDto> galleryItems = productService.getGalleryItems();
        return ResponseEntity.ok(galleryItems);
    }

    @GetMapping("/products")
    public ResponseEntity<PaginationResponse<ProductDto>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "8") Integer limit,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "created_at,desc") String sort) {

        PaginationResponse<ProductDto> productsWithPagination =
                productService.getProducts(categoryId, limit, page, sort);

        return ResponseEntity.ok(productsWithPagination);
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<ProductDto>> searchProducts(
            @RequestParam String query,
            @RequestParam(required = false) Long categoryId, // 카테고리 ID로 변경
            @RequestParam(required = false, defaultValue = "relevance") String sort,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {

        List<ProductDto> searchResults = productService.searchProducts(query, categoryId, sort, limit);
        return ResponseEntity.ok(searchResults);
    }

    @GetMapping("/products/new-arrivals")
    public ResponseEntity<List<ProductDto>> getNewArrivals(
            @RequestParam(required = false, defaultValue = "4") Integer limit) {

        List<ProductDto> newArrivals = productService.getNewArrivals(limit);
        return ResponseEntity.ok(newArrivals);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDetailDto> getProductDetail(@PathVariable Long productId) {
        ProductDetailDto productDetail = productService.getProductDetail(productId);
        return ResponseEntity.ok(productDetail);
    }

    @GetMapping("/products/{productId}/related")
    public ResponseEntity<RelatedProductResponseDto> getRelatedProducts(@PathVariable Long productId) {
        RelatedProductResponseDto relatedProducts = productService.getRelatedProducts(productId);
        return ResponseEntity.ok(relatedProducts);
    }
}