package com.example.iimt4601_backend.controller.admin;

import com.example.iimt4601_backend.dto.*;
import com.example.iimt4601_backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
//@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    @Autowired
    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    // 상품 조회
    @GetMapping
    public ResponseEntity<ProductListResponseDto> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "id,desc") String sort,
            @RequestParam(required = false) String search) {
        ProductListResponseDto products = productService.getProducts(page, size, category, status, sort, search);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    // 상품 생성
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto productDto) {
//        return new ResponseEntity<>(productService.createProduct(productDto), HttpStatus.CREATED);
        ProductResponseDto createdProduct = productService.createProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    // 상품 이미지 업로드
    @PostMapping("/images")
    public ResponseEntity<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = productService.uploadProductImage(file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    // 상품 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto productDto) {
        ProductResponseDto updatedProduct = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    // 상품 부분 업데이트
    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDto> partialUpdateProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        ProductResponseDto updatedProduct = productService.partialUpdateProduct(id, updates);
        return ResponseEntity.ok(updatedProduct);
    }

    // 상품 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // 상품 일괄 삭제
    @DeleteMapping("/batch")
    public ResponseEntity<Void> batchDeleteProducts(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            productService.deleteProduct(id);
        }
        return ResponseEntity.noContent().build();
    }
}