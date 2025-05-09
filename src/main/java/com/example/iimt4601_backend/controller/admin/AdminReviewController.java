package com.example.iimt4601_backend.controller.admin;

import com.example.iimt4601_backend.dto.AdminReviewListResponseDto;
import com.example.iimt4601_backend.dto.ReviewDetailDto;
import com.example.iimt4601_backend.dto.ReviewListResponseDto;
import com.example.iimt4601_backend.dto.ReviewVisibilityDto;
import com.example.iimt4601_backend.service.AdminReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final AdminReviewService reviewService;

    @GetMapping
    public ResponseEntity<AdminReviewListResponseDto> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Boolean visible,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        AdminReviewListResponseDto reviews = reviewService.getReviews(page, size, productId, visible, sort);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDetailDto> getReviewById(@PathVariable Long id) {
        ReviewDetailDto review = reviewService.getReviewById(id);
        return ResponseEntity.ok(review);
    }

    @PatchMapping("/{id}/visibility")
    public ResponseEntity<ReviewDetailDto> updateReviewVisibility(
            @PathVariable Long id,
            @Valid @RequestBody ReviewVisibilityDto visibilityDto) {
        ReviewDetailDto updatedReview = reviewService.updateReviewVisibility(id, visibilityDto);
        return ResponseEntity.ok(updatedReview);
    }
}